package com.docmanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleFriendApplyRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String replyMessage;
}
