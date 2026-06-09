package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        return ApiResponse.success(userService.getProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(request);
        return ApiResponse.success("修改成功");
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("密码修改成功，请重新登录");
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AvatarUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("上传成功", userService.uploadAvatar(file));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailVO> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }
}
