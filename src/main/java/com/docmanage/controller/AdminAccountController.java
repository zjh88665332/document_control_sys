package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AdminUserListItemVO>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminUserService.listAdmins(username, realName, phone, status, pageNum, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateAdminRequest request) {
        adminUserService.createAdmin(request);
        return ApiResponse.success("创建成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminUserService.deleteAdmin(id);
        return ApiResponse.success("已禁用该管理员");
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return ApiResponse.success("密码已重置为123456");
    }
}
