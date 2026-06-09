package com.docmanage.dto;

import com.docmanage.entity.Feedback;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminFeedbackListItemVO {

    private Long id;
    private String subject;
    private String content;
    private String submitterName;
    private String submitterPhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    private String reply;
    private Integer status;

    public static AdminFeedbackListItemVO from(Feedback feedback, String phone) {
        return AdminFeedbackListItemVO.builder()
                .id(feedback.getId())
                .subject(feedback.getSubject())
                .content(feedback.getContent())
                .submitterName(feedback.getSubmitterName())
                .submitterPhone(phone)
                .submitTime(feedback.getSubmitTime())
                .reply(feedback.getReply())
                .status(feedback.getStatus())
                .build();
    }
}
