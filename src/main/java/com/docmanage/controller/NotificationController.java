package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.MenuBadgeVO;
import com.docmanage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/badges")
    public ApiResponse<MenuBadgeVO> badges() {
        return ApiResponse.success(notificationService.getBadges());
    }

    @PostMapping("/read/{type}")
    public ApiResponse<Void> markRead(@PathVariable String type) {
        notificationService.markRead(type);
        return ApiResponse.success("已标记为已读");
    }
}
