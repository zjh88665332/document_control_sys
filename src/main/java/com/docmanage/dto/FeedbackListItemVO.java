package com.docmanage.dto;

import com.docmanage.entity.Feedback;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackListItemVO {

    private Long id;
    private String subject;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    private String reply;
    private Integer status;

    public static FeedbackListItemVO from(Feedback feedback) {
        return FeedbackListItemVO.builder()
                .id(feedback.getId())
                .subject(feedback.getSubject())
                .content(feedback.getContent())
                .submitTime(feedback.getSubmitTime())
                .reply(feedback.getReply())
                .status(feedback.getStatus())
                .build();
    }
}
