package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_file")
public class DocFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_uuid", nullable = false, unique = true, length = 100)
    private String fileUuid;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String format;

    @Column(nullable = false)
    private Long size = 0L;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 0;

    @Column(name = "is_audit_read", nullable = false, columnDefinition = "TINYINT")
    private Integer isAuditRead = 1;

    @Column(length = 255)
    private String remark;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "audit_by")
    private Long auditBy;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT")
    private Integer isDeleted = 0;

    @Column(name = "delete_time")
    private LocalDateTime deleteTime;

    @PrePersist
    public void prePersist() {
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
    }
}
