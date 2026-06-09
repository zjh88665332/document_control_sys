package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String salt;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "id_card", length = 18)
    private String idCard;

    @Column(columnDefinition = "TINYINT")
    private Integer gender = 0;

    private LocalDate birthday;

    @Column(length = 20)
    private String education;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(length = 20)
    private String identity;

    @Column(nullable = false, length = 20)
    private String role = "user";

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;

    @Column(length = 255)
    private String avatar;

    @Column(name = "last_notice_read_time")
    private LocalDateTime lastNoticeReadTime;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return "admin".equals(role) || "super".equals(role);
    }
}
