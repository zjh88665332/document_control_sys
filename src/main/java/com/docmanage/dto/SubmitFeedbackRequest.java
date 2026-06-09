package com.docmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitFeedbackRequest {

    @NotBlank(message = "反馈主题不能为空")
    private String subject;

    @NotBlank(message = "反馈内容不能为空")
    private String content;
}
