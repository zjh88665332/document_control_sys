package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.AdminFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/file")
@RequiredArgsConstructor
public class AdminFileController {

    private final AdminFileService adminFileService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AdminFileListItemVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long uploaderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminFileService.listFiles(name, status, uploaderId, pageNum, pageSize));
    }

    @GetMapping("/pending-count")
    public ApiResponse<Long> pendingCount() {
        return ApiResponse.success(adminFileService.pendingCount());
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditFileRequest request) {
        adminFileService.auditFile(id, request);
        return ApiResponse.success("审核成功");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return adminFileService.download(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminFileService.adminDeleteFile(id);
        return ApiResponse.success("删除成功");
    }
}
