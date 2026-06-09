package com.docmanage.dto;

import com.docmanage.entity.Friend;
import com.docmanage.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceivedApplyVO {

    private Long id;
    private String applicantName;
    private String applicantPhone;
    private Integer status;
    private String applyMessage;
    private String replyMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    public static ReceivedApplyVO from(Friend friend, User applicant) {
        return ReceivedApplyVO.builder()
                .id(friend.getId())
                .applicantName(applicant != null ? defaultName(applicant) : "未知用户")
                .applicantPhone(applicant != null ? applicant.getPhone() : "-")
                .status(friend.getStatus())
                .applyMessage(friend.getApplyMessage())
                .replyMessage(friend.getReplyMessage())
                .applyTime(friend.getApplyTime())
                .build();
    }

    private static String defaultName(User user) {
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
