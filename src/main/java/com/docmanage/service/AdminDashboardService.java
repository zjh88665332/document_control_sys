package com.docmanage.service;

import com.docmanage.dto.DashboardOverviewVO;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.FeedbackRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.service.support.AdminAuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final DocFileRepository docFileRepository;
    private final FeedbackRepository feedbackRepository;
    private final AdminAuthSupport adminAuth;

    @Transactional(readOnly = true)
    public DashboardOverviewVO getOverview() {
        adminAuth.requireAdmin();

        return DashboardOverviewVO.builder()
                .userCount(userRepository.countByRole("user"))
                .adminCount(userRepository.countByRole("admin"))
                .pendingFileCount(docFileRepository.countByIsDeletedAndStatus(0, 0))
                .pendingFeedbackCount(feedbackRepository.countByStatus(0))
                .totalFileCount(docFileRepository.countByIsDeleted(0))
                .approvedFileCount(docFileRepository.countByIsDeletedAndStatus(0, 1))
                .build();
    }
}
