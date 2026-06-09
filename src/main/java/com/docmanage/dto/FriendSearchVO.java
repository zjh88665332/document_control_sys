package com.docmanage.dto;

import com.docmanage.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendSearchVO {

    private Long id;
    private String realName;
    private String phone;

    public static FriendSearchVO from(User user) {
        return FriendSearchVO.builder()
                .id(user.getId())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .build();
    }
}
