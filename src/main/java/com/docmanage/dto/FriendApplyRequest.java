package com.docmanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendApplyRequest {

    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    private String applyMessage;
}
