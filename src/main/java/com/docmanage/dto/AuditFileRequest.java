package com.docmanage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditFileRequest {

    @NotNull(message = "审核状态不能为空")
    @Min(1)
    @Max(2)
    private Integer status;

    @Size(max = 500, message = "驳回原因不能超过500个字符")
    private String rejectReason;
}
