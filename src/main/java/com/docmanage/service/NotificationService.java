package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.MenuBadgeVO;
import com.docmanage.entity.User;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.FeedbackRepository;
import com.docmanage.repository.FriendRepository;
import com.docmanage.repository.NoticeRepository;
import com.docmanage.repository.ShareRecordRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REPLIED = 1;
    private static final int STATUS_ACTIVE = 1;
    private static final int UNREAD = 0;

    private final FriendRepository friendRepository;
    private final ShareRecordRepository shareRecordRepository;
    private final FeedbackRepository feedbackRepository;
    private final DocFileRepository docFileRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MenuBadgeVO getBadges() {
        User user = findCurrentUser();
        Long userId = user.getId();

        boolean friend = friendRepository.countByFriendIdAndStatusAndIsDelAndIsRead(
                userId, STATUS_PENDING, 0, UNREAD) > 0;
        boolean share = shareRecordRepository.countByReceiverIdAndStatusAndIsRead(
                userId, STATUS_ACTIVE, UNREAD) > 0;
        boolean feedback = feedbackRepository.countBySubmitterIdAndStatusAndIsReplyRead(
                userId, STATUS_REPLIED, UNREAD) > 0;
        boolean file = docFileRepository.countUnreadAuditResults(userId) > 0;

        LocalDateTime lastNoticeReadTime = user.getLastNoticeReadTime();
        boolean notice = lastNoticeReadTime == null
                ? noticeRepository.countByStatus(STATUS_APPROVED) > 0
                : noticeRepository.countByStatusAndPublishTimeAfter(STATUS_APPROVED, lastNoticeReadTime) > 0;

        return MenuBadgeVO.builder()
                .friend(friend)
                .share(share)
                .feedback(feedback)
                .file(file)
                .notice(notice)
                .build();
    }

    @Transactional
    public void markRead(String type) {
        User user = findCurrentUser();
        Long userId = user.getId();

        switch (type) {
            case "friend" -> friendRepository.markReceivedAppliesRead(userId);
            case "share" -> shareRecordRepository.markReceivedSharesRead(userId);
            case "feedback" -> feedbackRepository.markRepliesRead(userId);
            case "file" -> docFileRepository.markAuditResultsRead(userId);
            case "notice" -> {
                user.setLastNoticeReadTime(LocalDateTime.now());
                userRepository.save(user);
            }
            default -> throw new BusinessException("无效的通知类型");
        }
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
