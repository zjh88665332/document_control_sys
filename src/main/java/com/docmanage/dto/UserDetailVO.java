package com.docmanage.dto;

import com.docmanage.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserDetailVO {

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
    private String role;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public static UserDetailVO from(User user) {
        return UserDetailVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .idCard(user.getIdCard())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .education(user.getEducation())
                .phone(user.getPhone())
                .identity(user.getIdentity())
                .role(user.getRole())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
