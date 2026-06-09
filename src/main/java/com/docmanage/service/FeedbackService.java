package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.FeedbackListItemVO;
import com.docmanage.dto.PageResult;
import com.docmanage.dto.SubmitFeedbackRequest;
import com.docmanage.entity.Feedback;
import com.docmanage.entity.User;
import com.docmanage.repository.FeedbackRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitFeedback(SubmitFeedbackRequest request) {
        User user = findCurrentUser();

        Feedback feedback = new Feedback();
        feedback.setSubject(request.getSubject());
        feedback.setContent(request.getContent());
        feedback.setSubmitterId(user.getId());
        feedback.setSubmitterName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        feedback.setStatus(0);

        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public PageResult<FeedbackListItemVO> listMyFeedbacks(int pageNum, int pageSize) {
        User user = findCurrentUser();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Feedback> page = feedbackRepository.findBySubmitterIdOrderBySubmitTimeDesc(user.getId(), pageable);
        return PageResult.from(page, FeedbackListItemVO::from);
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
