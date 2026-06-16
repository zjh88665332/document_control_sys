package com.docmanage.dto;

import com.docmanage.entity.DocFile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminFileListItemVO {

    private Long id;
    private String name;
    private String format;
    private Long size;
    private Integer status;
    private String remark;
    private String tags;
    private String auditRejectReason;
    private Long uploaderId;
    private String uploaderName;
    private String uploaderPhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    public static AdminFileListItemVO from(DocFile file, String uploaderName, String uploaderPhone) {
        return AdminFileListItemVO.builder()
                .id(file.getId())
                .name(file.getName())
                .format(file.getFormat())
                .size(file.getSize())
                .status(file.getStatus())
                .remark(file.getRemark())
                .tags(file.getTags())
                .auditRejectReason(file.getAuditRejectReason())
                .uploaderId(file.getUploaderId())
                .uploaderName(uploaderName)
                .uploaderPhone(uploaderPhone)
                .uploadTime(file.getUploadTime())
                .auditTime(file.getAuditTime())
                .build();
    }
}
