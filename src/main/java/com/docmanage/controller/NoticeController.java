package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.NoticeDetailVO;
import com.docmanage.dto.NoticeListItemVO;
import com.docmanage.dto.PageResult;
import com.docmanage.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public ApiResponse<PageResult<NoticeListItemVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(noticeService.listPublishedNotices(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getNoticeDetail(id));
    }
}
