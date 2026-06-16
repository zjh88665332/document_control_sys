package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.CreateFolderRequest;
import com.docmanage.dto.FolderVO;
import com.docmanage.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/tree")
    public ApiResponse<List<FolderVO>> tree() {
        return ApiResponse.success(folderService.getFolderTree());
    }

    @PostMapping
    public ApiResponse<FolderVO> create(@Valid @RequestBody CreateFolderRequest request) {
        return ApiResponse.success("创建成功", folderService.createFolder(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ApiResponse.success("删除成功");
    }
}
