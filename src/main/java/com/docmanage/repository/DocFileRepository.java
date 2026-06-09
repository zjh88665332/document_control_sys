package com.docmanage.repository;

import com.docmanage.entity.DocFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocFileRepository extends JpaRepository<DocFile, Long> {

    @Query("SELECT f FROM DocFile f WHERE f.uploaderId = :uploaderId AND f.isDeleted = 0 " +
            "AND (:name IS NULL OR f.name LIKE CONCAT('%', :name, '%')) " +
            "ORDER BY f.uploadTime DESC")
    Page<DocFile> findByUploaderAndName(@Param("uploaderId") Long uploaderId,
                                        @Param("name") String name,
                                        Pageable pageable);

    Optional<DocFile> findByIdAndUploaderIdAndIsDeleted(Long id, Long uploaderId, Integer isDeleted);

    List<DocFile> findByUploaderIdAndIsDeletedAndStatus(Long uploaderId, Integer isDeleted, Integer status);

    long countByIsDeleted(Integer isDeleted);

    long countByIsDeletedAndStatus(Integer isDeleted, Integer status);

    @Query("SELECT f FROM DocFile f WHERE f.isDeleted = 0 " +
            "AND (:name IS NULL OR f.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:uploaderId IS NULL OR f.uploaderId = :uploaderId) " +
            "ORDER BY f.uploadTime DESC")
    Page<DocFile> findAdminFiles(@Param("name") String name,
                                   @Param("status") Integer status,
                                   @Param("uploaderId") Long uploaderId,
                                   Pageable pageable);

    Optional<DocFile> findByIdAndIsDeleted(Long id, Integer isDeleted);

    long countByUploaderIdAndIsDeletedAndStatusAndIsAuditRead(Long uploaderId, Integer isDeleted,
                                                              Integer status, Integer isAuditRead);

    @Modifying
    @Query("UPDATE DocFile f SET f.isAuditRead = 1 WHERE f.uploaderId = :userId AND f.isDeleted = 0 " +
            "AND f.status = 1 AND f.isAuditRead = 0")
    void markAuditApprovedRead(@Param("userId") Long userId);
}
