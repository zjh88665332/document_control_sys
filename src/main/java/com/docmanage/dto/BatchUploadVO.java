package com.docmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchUploadVO {

    private int successCount;
    private int failCount;
}
