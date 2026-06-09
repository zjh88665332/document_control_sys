package com.docmanage.repository;

import com.docmanage.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findBySubmitterIdOrderBySubmitTimeDesc(Long submitterId, Pageable pageable);

    long countByStatus(Integer status);

    @Query("SELECT f FROM Feedback f WHERE " +
            "(:status IS NULL OR f.status = :status) " +
            "AND (:subject IS NULL OR f.subject LIKE CONCAT('%', :subject, '%')) " +
            "ORDER BY f.submitTime DESC")
    Page<Feedback> findAdminFeedbacks(@Param("status") Integer status,
                                      @Param("subject") String subject,
                                      Pageable pageable);

    long countBySubmitterIdAndStatusAndIsReplyRead(Long submitterId, Integer status, Integer isReplyRead);

    @Modifying
    @Query("UPDATE Feedback f SET f.isReplyRead = 1 WHERE f.submitterId = :userId AND f.status = 1 AND f.isReplyRead = 0")
    void markRepliesRead(@Param("userId") Long userId);
}
