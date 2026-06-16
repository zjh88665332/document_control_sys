package com.docmanage.service;

import com.docmanage.dto.DashboardChartsVO;
import com.docmanage.dto.DashboardOverviewVO;
import com.docmanage.entity.User;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.FeedbackRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.service.support.AdminAuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public DashboardChartsVO getCharts() {
        adminAuth.requireAdmin();

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            dates.add(day.format(formatter));
            counts.add(docFileRepository.countUploadsBetween(start, end));
        }

        List<Object[]> topUploaders = docFileRepository.findTopUploaders(PageRequest.of(0, 5));
        List<Long> uploaderIds = topUploaders.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
        Map<Long, User> userMap = userRepository.findAllById(uploaderIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<String> topNames = new ArrayList<>();
        List<Long> topCounts = new ArrayList<>();
        for (Object[] row : topUploaders) {
            Long uploaderId = ((Number) row[0]).longValue();
            User user = userMap.get(uploaderId);
            topNames.add(user != null ? user.getRealName() : "用户" + uploaderId);
            topCounts.add(((Number) row[1]).longValue());
        }

        return DashboardChartsVO.builder()
                .uploadTrendDates(dates)
                .uploadTrendCounts(counts)
                .approvedCount(docFileRepository.countByIsDeletedAndStatus(0, 1))
                .rejectedCount(docFileRepository.countByIsDeletedAndStatus(0, 2))
                .pendingCount(docFileRepository.countByIsDeletedAndStatus(0, 0))
                .topUploaderNames(topNames)
                .topUploaderCounts(topCounts)
                .build();
    }
}
