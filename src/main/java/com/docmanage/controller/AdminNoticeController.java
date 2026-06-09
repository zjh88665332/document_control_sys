package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AdminNoticeListItemVO>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(noticeService.listAdminNotices(title, status, pageNum, pageSize));
    }

    @PostMapping
    public ApiResponse<CreateNoticeResponse> create(@Valid @RequestBody CreateNoticeRequest request) {
        return ApiResponse.success("发布成功", noticeService.createNotice(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateNoticeRequest request) {
        noticeService.updateNotice(id, request);
        return ApiResponse.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ApiResponse.success("删除成功");
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteNoticeRequest request) {
        noticeService.batchDeleteNotices(request);
        return ApiResponse.success("批量删除成功");
    }
}
