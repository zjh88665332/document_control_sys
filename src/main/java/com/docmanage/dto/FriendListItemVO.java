package com.docmanage.dto;

import com.docmanage.entity.Friend;
import com.docmanage.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class FriendListItemVO {

    private Long id;
    private String realName;
    private String phone;
    private Long addTime;

    public static FriendListItemVO from(Friend friend, User friendUser) {
        LocalDateTime time = friend.getHandleTime() != null ? friend.getHandleTime() : friend.getApplyTime();
        return FriendListItemVO.builder()
                .id(friendUser.getId())
                .realName(friendUser.getRealName())
                .phone(friendUser.getPhone())
                .addTime(time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }
}
