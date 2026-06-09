package com.docmanage.dto;

import com.docmanage.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileVO {

    private Long id;
    private String username;
    private String realName;
    private String idCard;
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String education;
    private String phone;
    private String identity;
    private String avatar;

    public static UserProfileVO from(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .idCard(user.getIdCard())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .education(user.getEducation())
                .phone(user.getPhone())
                .identity(user.getIdentity())
                .avatar(user.getAvatar())
                .build();
    }
}
