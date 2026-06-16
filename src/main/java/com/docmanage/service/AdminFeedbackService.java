package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.AdminFeedbackListItemVO;
import com.docmanage.dto.PageResult;
import com.docmanage.dto.ReplyFeedbackRequest;
import com.docmanage.dto.WsNotificationMessage;
import com.docmanage.entity.Feedback;
import com.docmanage.entity.User;
import com.docmanage.repository.FeedbackRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.service.support.AdminAuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final AdminAuthSupport adminAuth;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional(readOnly = true)
    public PageResult<AdminFeedbackListItemVO> listFeedbacks(Integer status, String subject,
                                                             int pageNum, int pageSize) {
        adminAuth.requireAdmin();

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Feedback> page = feedbackRepository.findAdminFeedbacks(
                status,
                StringUtils.hasText(subject) ? subject : null,
                pageable
        );

        Map<Long, User> submitterMap = loadSubmitters(page.getContent());

        List<AdminFeedbackListItemVO> list = page.getContent().stream()
                .map(f -> {
                    User submitter = submitterMap.get(f.getSubmitterId());
                    String phone = submitter != null ? submitter.getPhone() : null;
                    return AdminFeedbackListItemVO.from(f, phone);
                })
                .collect(Collectors.toList());

        return PageResult.<AdminFeedbackListItemVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    @Transactional
    public void replyFeedback(Long id, ReplyFeedbackRequest request) {
        User operator = adminAuth.requireAdmin();
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "反馈不存在"));

        feedback.setReply(request.getReply());
        feedback.setReplyBy(operator.getId());
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(1);
        feedback.setIsReplyRead(0);
        feedbackRepository.save(feedback);
        webSocketNotificationService.pushToUser(feedback.getSubmitterId(), WsNotificationMessage.builder()
                .type("feedback")
                .title("反馈已回复")
                .content("您的反馈「" + feedback.getSubject() + "」已收到回复")
                .targetId(feedback.getId())
                .build());
    }

    private Map<Long, User> loadSubmitters(List<Feedback> feedbacks) {
        List<Long> ids = feedbacks.stream().map(Feedback::getSubmitterId).distinct().toList();
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
