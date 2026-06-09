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
            @RequestParam(value = "remark", required = false) String remark) {
        FileUploadVO result = docFileService.upload(file, name, remark);
        return ApiResponse.success("上传成功，等待审核", result);
    }

    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BatchUploadVO> batchUpload(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success("批量上传成功", docFileService.batchUpload(files));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<FileListItemVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(docFileService.listMyFiles(name, pageNum, pageSize));
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
