package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "publisher_name", length = 50)
    private String publisherName;

    @Column(name = "is_top", nullable = false, columnDefinition = "TINYINT")
    private Integer isTop = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "publish_time", nullable = false)
    private LocalDateTime publishTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (publishTime == null) {
            publishTime = now;
        }
    }
}
