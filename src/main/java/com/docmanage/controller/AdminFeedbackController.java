package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.AdminFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AdminFeedbackListItemVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminFeedbackService.listFeedbacks(status, subject, pageNum, pageSize));
    }

    @PutMapping("/{id}/reply")
    public ApiResponse<Void> reply(@PathVariable Long id, @Valid @RequestBody ReplyFeedbackRequest request) {
        adminFeedbackService.replyFeedback(id, request);
        return ApiResponse.success("回复成功");
    }
}
