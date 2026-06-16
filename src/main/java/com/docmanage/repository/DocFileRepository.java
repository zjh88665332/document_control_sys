package com.docmanage.repository;

import com.docmanage.entity.DocFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocFileRepository extends JpaRepository<DocFile, Long> {

    @Query("SELECT f FROM DocFile f WHERE f.uploaderId = :uploaderId AND f.isDeleted = 0 " +
            "AND (:folderId IS NULL OR f.folderId = :folderId OR (:folderId = 0 AND f.folderId IS NULL)) " +
            "AND (:keyword IS NULL OR f.name LIKE CONCAT('%', :keyword, '%') " +
            "OR f.tags LIKE CONCAT('%', :keyword, '%') OR f.remark LIKE CONCAT('%', :keyword, '%') " +
            "OR f.searchContent LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY f.uploadTime DESC")
    Page<DocFile> findByUploaderAndKeyword(@Param("uploaderId") Long uploaderId,
                                           @Param("folderId") Long folderId,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    @Query("SELECT f FROM DocFile f WHERE f.uploaderId = :uploaderId AND f.isDeleted = 1 " +
            "ORDER BY f.deleteTime DESC")
    Page<DocFile> findRecycleBin(@Param("uploaderId") Long uploaderId, Pageable pageable);

    Optional<DocFile> findByIdAndUploaderIdAndIsDeleted(Long id, Long uploaderId, Integer isDeleted);

    long countByFolderIdAndIsDeleted(Long folderId, Integer isDeleted);

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

    @Query("SELECT COUNT(f) FROM DocFile f WHERE f.uploaderId = :uploaderId AND f.isDeleted = 0 " +
            "AND f.status IN (1, 2) AND f.isAuditRead = 0")
    long countUnreadAuditResults(@Param("uploaderId") Long uploaderId);

    @Modifying
    @Query("UPDATE DocFile f SET f.isAuditRead = 1 WHERE f.uploaderId = :userId AND f.isDeleted = 0 " +
            "AND f.status IN (1, 2) AND f.isAuditRead = 0")
    void markAuditResultsRead(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM DocFile f WHERE f.isDeleted = 0 AND f.uploadTime >= :start AND f.uploadTime < :end")
    long countUploadsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT f.uploaderId, COUNT(f) FROM DocFile f WHERE f.isDeleted = 0 GROUP BY f.uploaderId ORDER BY COUNT(f) DESC")
    List<Object[]> findTopUploaders(Pageable pageable);
}
