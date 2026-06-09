package com.docmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "b_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Column(name = "submitter_name", length = 50)
    private String submitterName;

    @Column(name = "submit_time", nullable = false)
    private LocalDateTime submitTime;

    @Column(columnDefinition = "TEXT")
    private String reply;

    @Column(name = "reply_by")
    private Long replyBy;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer status = 0;

    @Column(name = "is_reply_read", nullable = false, columnDefinition = "TINYINT")
    private Integer isReplyRead = 1;

    @PrePersist
    public void prePersist() {
        if (submitTime == null) {
            submitTime = LocalDateTime.now();
        }
    }
}
