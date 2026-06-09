package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_friend")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 0;

    @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT")
    private Integer isRead = 0;

    @Column(name = "apply_message", length = 255)
    private String applyMessage;

    @Column(name = "reply_message", length = 255)
    private String replyMessage;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "is_del", nullable = false, columnDefinition = "TINYINT")
    private Integer isDel = 0;

    @PrePersist
    public void prePersist() {
        if (applyTime == null) {
            applyTime = LocalDateTime.now();
        }
    }
}
