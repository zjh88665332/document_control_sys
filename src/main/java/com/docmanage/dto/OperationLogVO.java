package com.docmanage.dto;

import com.docmanage.entity.OperationLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperationLogVO {

    private Long id;
    private String username;
    private String module;
    private String action;
    private String targetName;
    private String detail;
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public static OperationLogVO from(OperationLog log) {
        return OperationLogVO.builder()
                .id(log.getId())
                .username(log.getUsername())
                .module(log.getModule())
                .action(log.getAction())
                .targetName(log.getTargetName())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createTime(log.getCreateTime())
                .build();
    }
}
