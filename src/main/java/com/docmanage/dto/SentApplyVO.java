package com.docmanage.dto;

import com.docmanage.entity.Friend;
import com.docmanage.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SentApplyVO {

    private Long id;
    private String targetName;
    private String targetPhone;
    private Integer status;
    private String applyMessage;
    private String replyMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    public static SentApplyVO from(Friend friend, User target) {
        return SentApplyVO.builder()
                .id(friend.getId())
                .targetName(target != null ? defaultName(target) : "未知用户")
                .targetPhone(target != null ? target.getPhone() : "-")
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
