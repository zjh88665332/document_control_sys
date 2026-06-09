package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/search")
    public ApiResponse<List<FriendSearchVO>> search(@RequestParam String keyword) {
        return ApiResponse.success(friendService.searchFriends(keyword));
    }

    @PostMapping("/apply")
    public ApiResponse<Void> apply(@Valid @RequestBody FriendApplyRequest request) {
        friendService.sendApply(request);
        return ApiResponse.success("申请已发送");
    }

    @GetMapping("/apply/received")
    public ApiResponse<PageResult<ReceivedApplyVO>> receivedApplies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(friendService.listReceivedApplies(pageNum, pageSize));
    }

    @PutMapping("/apply/{id}/handle")
    public ApiResponse<Void> handleApply(
            @PathVariable Long id,
            @Valid @RequestBody HandleFriendApplyRequest request) {
        friendService.handleApply(id, request);
        return ApiResponse.success("处理成功");
    }

    @GetMapping("/apply/sent")
    public ApiResponse<PageResult<SentApplyVO>> sentApplies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(friendService.listSentApplies(pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FriendListItemVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(friendService.listFriends(name, phone, pageNum, pageSize));
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
        return ApiResponse.success("删除成功");
    }
}
