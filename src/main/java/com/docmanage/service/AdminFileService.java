package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.AdminFileListItemVO;
import com.docmanage.dto.AuditFileRequest;
import com.docmanage.dto.PageResult;
import com.docmanage.dto.WsNotificationMessage;
import com.docmanage.entity.DocFile;
import com.docmanage.entity.User;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.ShareRecordRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.service.support.AdminAuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFileService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;

    private final DocFileRepository docFileRepository;
    private final ShareRecordRepository shareRecordRepository;
    private final UserRepository userRepository;
    private static final int STATUS_REJECTED = 2;

    private final AdminAuthSupport adminAuth;
    private final DocFileService docFileService;
    private final OperationLogService operationLogService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional(readOnly = true)
    public PageResult<AdminFileListItemVO> listFiles(String name, Integer status, Long uploaderId,
                                                     int pageNum, int pageSize) {
        adminAuth.requireAdmin();

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<DocFile> page = docFileRepository.findAdminFiles(
                StringUtils.hasText(name) ? name : null,
                status,
                uploaderId,
                pageable
        );

        Map<Long, User> uploaderMap = loadUploaders(page.getContent());

        List<AdminFileListItemVO> list = page.getContent().stream()
                .map(file -> {
                    User uploader = uploaderMap.get(file.getUploaderId());
                    String uploaderName = uploader != null ? uploader.getRealName() : null;
                    String uploaderPhone = uploader != null ? uploader.getPhone() : null;
                    return AdminFileListItemVO.from(file, uploaderName, uploaderPhone);
                })
                .collect(Collectors.toList());

        return PageResult.<AdminFileListItemVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        adminAuth.requireAdmin();
        return docFileRepository.countByIsDeletedAndStatus(0, STATUS_PENDING);
    }

    @Transactional
    public void auditFile(Long id, AuditFileRequest request) {
        User auditor = adminAuth.requireAdmin();
        DocFile file = docFileRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        if (file.getStatus() == null || file.getStatus() != STATUS_PENDING) {
            throw new BusinessException(400, "该文件已审核");
        }

        if (request.getStatus() != null && request.getStatus() == STATUS_REJECTED) {
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BusinessException("请填写驳回原因");
            }
            file.setAuditRejectReason(request.getRejectReason().trim());
        } else {
            file.setAuditRejectReason(null);
        }

        file.setStatus(request.getStatus());
        file.setAuditBy(auditor.getId());
        file.setAuditTime(LocalDateTime.now());
        file.setIsAuditRead(0);
        docFileRepository.save(file);

        String action = request.getStatus() == STATUS_APPROVED ? "审核通过" : "审核驳回";
        operationLogService.log("文件审核", action, file.getId(), file.getName(),
                request.getStatus() == STATUS_REJECTED ? request.getRejectReason() : "审核通过");

        String title = request.getStatus() == STATUS_APPROVED ? "文件审核通过" : "文件审核驳回";
        String content = request.getStatus() == STATUS_APPROVED
                ? "您的文件「" + file.getName() + "」已通过审核"
                : "您的文件「" + file.getName() + "」被驳回：" + request.getRejectReason();
        webSocketNotificationService.pushToUser(file.getUploaderId(), WsNotificationMessage.builder()
                .type("file")
                .title(title)
                .content(content)
                .targetId(file.getId())
                .build());
    }

    @Transactional
    public void adminDeleteFile(Long id) {
        adminAuth.requireAdmin();
        DocFile file = docFileRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        file.setIsDeleted(1);
        file.setDeleteTime(LocalDateTime.now());
        docFileRepository.save(file);

        shareRecordRepository.findByFileIdAndStatus(id, 1).forEach(share -> {
            share.setStatus(0);
            shareRecordRepository.save(share);
        });
        operationLogService.log("文件审核", "管理员删除", id, file.getName(), "管理员删除文件");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(Long id) {
        adminAuth.requireAdmin();
        DocFile file = docFileRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        return docFileService.buildDownloadResponse(file, true);
    }

    private Map<Long, User> loadUploaders(List<DocFile> files) {
        List<Long> ids = files.stream().map(DocFile::getUploaderId).distinct().toList();
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
