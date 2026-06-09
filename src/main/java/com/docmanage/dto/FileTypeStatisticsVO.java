package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileTypeStatisticsVO {

    private long document;
    private long image;
    private long video;
    private long compress;
}
