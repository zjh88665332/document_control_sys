package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_share")
public class ShareRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "sharer_id", nullable = false)
    private Long sharerId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(length = 255)
    private String remark;

    @Column(name = "share_time", nullable = false)
    private LocalDateTime shareTime;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;

    @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT")
    private Integer isRead = 0;

    @PrePersist
    public void prePersist() {
        if (shareTime == null) {
            shareTime = LocalDateTime.now();
        }
    }
}
