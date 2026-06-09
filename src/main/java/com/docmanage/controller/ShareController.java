package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.CreateShareRequest;
import com.docmanage.dto.PageResult;
import com.docmanage.dto.ReceivedShareVO;
import com.docmanage.dto.SentShareVO;
import com.docmanage.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public ApiResponse<Void> share(@Valid @RequestBody CreateShareRequest request) {
        shareService.shareFile(request);
        return ApiResponse.success("分享成功");
    }

    @GetMapping("/received")
    public ApiResponse<PageResult<ReceivedShareVO>> received(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(shareService.listReceived(pageNum, pageSize));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return shareService.downloadShared(id);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        return shareService.previewShared(id);
    }

    @GetMapping("/sent")
    public ApiResponse<PageResult<SentShareVO>> sent(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(shareService.listSent(pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        shareService.cancelShare(id);
        return ApiResponse.success("取消分享成功");
    }
}
