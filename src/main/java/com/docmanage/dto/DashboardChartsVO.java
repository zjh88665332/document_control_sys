package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardChartsVO {

    private List<String> uploadTrendDates;
    private List<Long> uploadTrendCounts;
    private long approvedCount;
    private long rejectedCount;
    private long pendingCount;
    private List<String> topUploaderNames;
    private List<Long> topUploaderCounts;
}
