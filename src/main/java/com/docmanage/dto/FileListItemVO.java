package com.docmanage.dto;

import com.docmanage.entity.DocFile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileListItemVO {

    private Long id;
    private String name;
    private String format;
    private Integer status;
    private String remark;
    private String tags;
    private Long folderId;
    private String auditRejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    public static FileListItemVO from(DocFile file) {
        return FileListItemVO.builder()
                .id(file.getId())
                .name(file.getName())
                .format(file.getFormat())
                .status(file.getStatus())
                .remark(file.getRemark())
                .tags(file.getTags())
                .folderId(file.getFolderId())
                .auditRejectReason(file.getAuditRejectReason())
                .uploadTime(file.getUploadTime())
                .deleteTime(file.getDeleteTime())
                .build();
    }
}
