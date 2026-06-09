package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.CreateShareRequest;
import com.docmanage.dto.PageResult;
import com.docmanage.dto.ReceivedShareVO;
import com.docmanage.dto.SentShareVO;
import com.docmanage.entity.DocFile;
import com.docmanage.entity.ShareRecord;
import com.docmanage.entity.User;
import com.docmanage.repository.ShareRecordRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import com.docmanage.util.FileTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareService {

    private static final int STATUS_ACTIVE = 1;

    private final ShareRecordRepository shareRecordRepository;
    private final UserRepository userRepository;
    private final DocFileService docFileService;
    private final FriendService friendService;

    @Transactional
    public void shareFile(CreateShareRequest request) {
        User user = findCurrentUser();

        DocFile file = docFileService.getApprovedFile(request.getFileId());
        if (!file.getUploaderId().equals(user.getId())) {
            throw new BusinessException(403, "只能分享自己上传的文件");
        }

        if (!friendService.isFriend(user.getId(), request.getReceiverId())) {
            throw new BusinessException("只能分享给好友");
        }

        userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException(404, "接收人不存在"));

        if (shareRecordRepository.existsByFileIdAndSharerIdAndReceiverIdAndStatus(
                request.getFileId(), user.getId(), request.getReceiverId(), STATUS_ACTIVE)) {
            throw new BusinessException("该文件已分享给此好友");
        }

        ShareRecord share = new ShareRecord();
        share.setFileId(request.getFileId());
        share.setSharerId(user.getId());
        share.setReceiverId(request.getReceiverId());
        share.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        share.setStatus(STATUS_ACTIVE);
        share.setIsRead(0);

        shareRecordRepository.save(share);
    }

    @Transactional(readOnly = true)
    public PageResult<ReceivedShareVO> listReceived(int pageNum, int pageSize) {
        User user = findCurrentUser();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<ShareRecord> page = shareRecordRepository.findByReceiverIdOrderByShareTimeDesc(user.getId(), pageable);
        return buildReceivedPage(page);
    }

    @Transactional
    public ResponseEntity<Resource> downloadShared(Long id) {
        User user = findCurrentUser();
        ShareRecord share = shareRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分享记录不存在"));

        if (share.getStatus() == null || share.getStatus() != STATUS_ACTIVE) {
            throw new BusinessException(400, "分享已失效");
        }

        if (!share.getReceiverId().equals(user.getId()) && !share.getSharerId().equals(user.getId())) {
            throw new BusinessException(403, "无权下载该分享");
        }

        DocFile file = docFileService.getApprovedFile(share.getFileId());
        if (share.getReceiverId().equals(user.getId())) {
            share.setIsRead(1);
            shareRecordRepository.save(share);
        }

        return docFileService.buildDownloadResponse(file);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> previewShared(Long id) {
        User user = findCurrentUser();
        ShareRecord share = shareRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分享记录不存在"));

        if (share.getStatus() == null || share.getStatus() != STATUS_ACTIVE) {
            throw new BusinessException(400, "分享已失效，无法预览");
        }

        if (!share.getReceiverId().equals(user.getId()) && !share.getSharerId().equals(user.getId())) {
            throw new BusinessException(403, "无权预览该分享");
        }

        DocFile file = docFileService.getApprovedFile(share.getFileId());
        String previewKey = StringUtils.hasText(file.getFormat()) ? file.getFormat() : file.getName();
        if ("unsupported".equals(FileTypeUtils.resolvePreviewType(previewKey))) {
            throw new BusinessException(400, "该文件类型暂不支持在线预览");
        }

        return docFileService.buildPreviewResponse(file);
    }

    @Transactional(readOnly = true)
    public PageResult<SentShareVO> listSent(int pageNum, int pageSize) {
        User user = findCurrentUser();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<ShareRecord> page = shareRecordRepository.findBySharerIdOrderByShareTimeDesc(user.getId(), pageable);
        return buildSentPage(page);
    }

    @Transactional
    public void cancelShare(Long id) {
        User user = findCurrentUser();
        ShareRecord share = shareRecordRepository.findByIdAndSharerId(id, user.getId())
                .orElseThrow(() -> new BusinessException(404, "分享记录不存在"));

        share.setStatus(0);
        shareRecordRepository.save(share);
    }

    private PageResult<ReceivedShareVO> buildReceivedPage(Page<ShareRecord> page) {
        List<Long> fileIds = page.getContent().stream().map(ShareRecord::getFileId).distinct().toList();
        List<Long> sharerIds = page.getContent().stream().map(ShareRecord::getSharerId).distinct().toList();

        Map<Long, DocFile> fileMap = docFileService.getFilesMap(fileIds);
        Map<Long, User> sharerMap = loadUsers(sharerIds);

        List<ReceivedShareVO> list = page.getContent().stream()
                .map(share -> {
                    DocFile file = fileMap.get(share.getFileId());
                    User sharer = sharerMap.get(share.getSharerId());
                    if (file == null || sharer == null) {
                        return null;
                    }
                    return ReceivedShareVO.from(share, file, sharer);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return PageResult.<ReceivedShareVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    private PageResult<SentShareVO> buildSentPage(Page<ShareRecord> page) {
        List<Long> fileIds = page.getContent().stream().map(ShareRecord::getFileId).distinct().toList();
        List<Long> receiverIds = page.getContent().stream().map(ShareRecord::getReceiverId).distinct().toList();

        Map<Long, DocFile> fileMap = docFileService.getFilesMap(fileIds);
        Map<Long, User> receiverMap = loadUsers(receiverIds);

        List<SentShareVO> list = page.getContent().stream()
                .map(share -> {
                    DocFile file = fileMap.get(share.getFileId());
                    User receiver = receiverMap.get(share.getReceiverId());
                    if (file == null || receiver == null) {
                        return null;
                    }
                    return SentShareVO.from(share, file, receiver);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return PageResult.<SentShareVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    private Map<Long, User> loadUsers(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
