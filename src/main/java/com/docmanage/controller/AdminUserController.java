package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AdminUserListItemVO>> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminUserService.listUsers(username, realName, phone, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateUserStatus(id, request);
        return ApiResponse.success("操作成功");
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return ApiResponse.success("密码已重置为123456");
    }
}
