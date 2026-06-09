package com.docmanage.repository;

import com.docmanage.entity.ShareRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShareRecordRepository extends JpaRepository<ShareRecord, Long> {

    Page<ShareRecord> findByReceiverIdOrderByShareTimeDesc(Long receiverId, Pageable pageable);

    Page<ShareRecord> findBySharerIdOrderByShareTimeDesc(Long sharerId, Pageable pageable);

    Optional<ShareRecord> findByIdAndReceiverIdAndStatus(Long id, Long receiverId, Integer status);

    Optional<ShareRecord> findByIdAndSharerId(Long id, Long sharerId);

    boolean existsByFileIdAndSharerIdAndReceiverIdAndStatus(Long fileId, Long sharerId, Long receiverId, Integer status);

    List<ShareRecord> findByFileIdAndStatus(Long fileId, Integer status);

    long countByReceiverIdAndStatusAndIsRead(Long receiverId, Integer status, Integer isRead);

    @Modifying
    @Query("UPDATE ShareRecord s SET s.isRead = 1 WHERE s.receiverId = :userId AND s.status = 1 AND s.isRead = 0")
    void markReceivedSharesRead(@Param("userId") Long userId);
}
