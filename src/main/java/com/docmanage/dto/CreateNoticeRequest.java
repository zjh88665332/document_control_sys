package com.docmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateNoticeRequest {

    @NotBlank(message = "公告主题不能为空")
    @Size(max = 200, message = "公告主题不能超过200字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private Integer isTop = 0;
}
