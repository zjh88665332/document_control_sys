package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewVO {

    private long userCount;
    private long adminCount;
    private long pendingFileCount;
    private long pendingFeedbackCount;
    private long totalFileCount;
    private long approvedFileCount;
}
