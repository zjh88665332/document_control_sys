package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ApiResponse<Void> submit(@Valid @RequestBody SubmitFeedbackRequest request) {
        feedbackService.submitFeedback(request);
        return ApiResponse.success("提交成功");
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FeedbackListItemVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(feedbackService.listMyFeedbacks(pageNum, pageSize));
    }
}
