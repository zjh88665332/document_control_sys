package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.*;
import com.docmanage.entity.Notice;
import com.docmanage.entity.User;
import com.docmanage.repository.NoticeRepository;
import com.docmanage.service.support.AdminAuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int STATUS_PUBLISHED = 1;

    private final NoticeRepository noticeRepository;
    private final AdminAuthSupport adminAuth;

    @Transactional(readOnly = true)
    public PageResult<NoticeListItemVO> listPublishedNotices(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Notice> page = noticeRepository.findByStatusOrderByIsTopDescPublishTimeDesc(STATUS_PUBLISHED, pageable);
        return PageResult.from(page, NoticeListItemVO::from);
    }

    @Transactional
    public NoticeDetailVO getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));

        if (notice.getStatus() == null || notice.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(404, "公告不存在");
        }

        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);

        return NoticeDetailVO.from(notice);
    }

    @Transactional(readOnly = true)
    public PageResult<AdminNoticeListItemVO> listAdminNotices(String title, Integer status, int pageNum, int pageSize) {
        adminAuth.requireAdmin();

        String titleFilter = StringUtils.hasText(title) ? title : null;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Notice> page = noticeRepository.findByFilters(titleFilter, status, pageable);
        return PageResult.from(page, AdminNoticeListItemVO::from);
    }

    @Transactional
    public CreateNoticeResponse createNotice(CreateNoticeRequest request) {
        User publisher = adminAuth.requireAdmin();

        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPublisherId(publisher.getId());
        notice.setPublisherName(resolvePublisherName(publisher));
        notice.setIsTop(request.getIsTop() != null ? request.getIsTop() : 0);
        notice.setStatus(STATUS_PUBLISHED);

        Notice saved = noticeRepository.save(notice);
        return new CreateNoticeResponse(saved.getId());
    }

    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        adminAuth.requireAdmin();

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));

        if (StringUtils.hasText(request.getTitle())) {
            notice.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            notice.setContent(request.getContent());
        }
        if (request.getIsTop() != null) {
            notice.setIsTop(request.getIsTop());
        }
        if (request.getStatus() != null) {
            notice.setStatus(request.getStatus());
        }

        noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        adminAuth.requireAdmin();

        if (!noticeRepository.existsById(id)) {
            throw new BusinessException(404, "公告不存在");
        }
        noticeRepository.deleteById(id);
    }

    @Transactional
    public void batchDeleteNotices(BatchDeleteNoticeRequest request) {
        adminAuth.requireAdmin();
        noticeRepository.deleteAllById(request.getIds());
    }

    private String resolvePublisherName(User user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if ("super".equals(user.getRole())) {
            return "超级管理员";
        }
        if ("admin".equals(user.getRole())) {
            return "管理员";
        }
        return user.getUsername();
    }
}
