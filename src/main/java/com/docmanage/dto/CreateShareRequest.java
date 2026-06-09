package com.docmanage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateShareRequest {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @Size(max = 255, message = "备注不能超过255字符")
    private String remark;
}
