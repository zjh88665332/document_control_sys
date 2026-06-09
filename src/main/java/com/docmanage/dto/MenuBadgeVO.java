package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuBadgeVO {

    private boolean friend;
    private boolean share;
    private boolean feedback;
    private boolean file;
    private boolean notice;
}
