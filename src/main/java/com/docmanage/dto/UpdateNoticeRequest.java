package com.docmanage.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateNoticeRequest {

    @Size(max = 200, message = "公告主题不能超过200字符")
    private String title;

    private String content;
    private Integer isTop;
    private Integer status;
}
