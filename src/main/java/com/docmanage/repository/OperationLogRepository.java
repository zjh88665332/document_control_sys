package com.docmanage.repository;

import com.docmanage.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    @Query("SELECT l FROM OperationLog l WHERE " +
            "(:module IS NULL OR l.module = :module) AND " +
            "(:username IS NULL OR l.username LIKE CONCAT('%', :username, '%')) " +
            "ORDER BY l.createTime DESC")
    Page<OperationLog> findByFilters(@Param("module") String module,
                                     @Param("username") String username,
                                     Pageable pageable);
}
