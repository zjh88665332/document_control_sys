package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.OperationLogVO;
import com.docmanage.dto.PageResult;
import com.docmanage.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/operation-log")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public ApiResponse<PageResult<OperationLogVO>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(operationLogService.listLogs(module, username, pageNum, pageSize));
    }
}
