package com.docmanage.controller;

import com.docmanage.common.ApiResponse;
import com.docmanage.dto.*;
import com.docmanage.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final DocFileService docFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        FileUploadVO result = docFileService.upload(file, name, remark, folderId);
        return ApiResponse.success("上传成功，等待审核", result);
    }

    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BatchUploadVO> batchUpload(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success("批量上传成功", docFileService.batchUpload(files));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FileListItemVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(docFileService.listMyFiles(keyword, folderId, pageNum, pageSize));
    }

    @GetMapping("/recycle/list")
    public ApiResponse<PageResult<FileListItemVO>> recycleList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(docFileService.listRecycleBin(pageNum, pageSize));
    }

    @PutMapping("/recycle/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        docFileService.restoreFile(id);
        return ApiResponse.success("恢复成功");
    }

    @DeleteMapping("/recycle/{id}")
    public ApiResponse<Void> permanentDelete(@PathVariable Long id) {
        docFileService.permanentDelete(id);
        return ApiResponse.success("彻底删除成功");
    }

    @PutMapping("/{id}/move")
    public ApiResponse<Void> move(@PathVariable Long id, @RequestBody MoveFileRequest request) {
        docFileService.moveFile(id, request.getFolderId());
        return ApiResponse.success("移动成功");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return docFileService.download(id);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        return docFileService.preview(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        docFileService.deleteFile(id);
        return ApiResponse.success("删除成功");
    }
}
