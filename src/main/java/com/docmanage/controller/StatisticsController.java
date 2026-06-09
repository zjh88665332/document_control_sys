package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.FileTypeStatisticsVO;
import com.docmanage.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final DocFileService docFileService;

    @GetMapping("/fileType")
    public ApiResponse<FileTypeStatisticsVO> fileTypeStatistics() {
        return ApiResponse.success(docFileService.fileTypeStatistics());
    }
}
