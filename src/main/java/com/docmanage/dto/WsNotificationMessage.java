package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WsNotificationMessage {

    private String type;
    private String title;
    private String content;
    private Long targetId;
}
