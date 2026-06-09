package com.docmanage.repository;

import com.docmanage.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByStatusOrderByIsTopDescPublishTimeDesc(Integer status, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE " +
            "(:title IS NULL OR n.title LIKE CONCAT('%', :title, '%')) AND " +
            "(:status IS NULL OR n.status = :status) " +
            "ORDER BY n.isTop DESC, n.publishTime DESC")
    Page<Notice> findByFilters(@Param("title") String title,
                               @Param("status") Integer status,
                               Pageable pageable);

    long countByStatus(Integer status);

    long countByStatusAndPublishTimeAfter(Integer status, java.time.LocalDateTime publishTime);
}
