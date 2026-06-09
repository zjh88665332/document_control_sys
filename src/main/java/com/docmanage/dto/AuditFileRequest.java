package com.docmanage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditFileRequest {

    @NotNull(message = "审核状态不能为空")
    @Min(1)
    @Max(2)
    private Integer status;
}
