package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.*;
import com.docmanage.entity.DocFile;
import com.docmanage.entity.User;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.ShareRecordRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import com.docmanage.util.FileTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocFileService {

    private static final int STATUS_APPROVED = 1;

    private final DocFileRepository docFileRepository;
    private final ShareRecordRepository shareRecordRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public FileUploadVO upload(MultipartFile file, String customName, String remark) {
        User user = findCurrentUser();
        FileStorageService.StoredFile stored = fileStorageService.storeDocument(file);

        DocFile docFile = new DocFile();
        docFile.setFileUuid(UUID.randomUUID().toString().replace("-", ""));
        docFile.setName(resolveDisplayName(customName, stored.originalName(), stored.format()));
        docFile.setFormat(stored.format());
        docFile.setSize(stored.size());
        docFile.setPath(stored.path());
        docFile.setRemark(remark);
        docFile.setUploaderId(user.getId());
        docFile.setStatus(0);
        docFile.setIsAuditRead(1);
        docFile.setIsDeleted(0);

        DocFile saved = docFileRepository.save(docFile);
        return new FileUploadVO(saved.getId(), saved.getName());
    }

    @Transactional
    public BatchUploadVO batchUpload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择要上传的文件");
        }

        int successCount = 0;
        int failCount = 0;
        for (MultipartFile file : files) {
            try {
                upload(file, null, null);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }
        return new BatchUploadVO(successCount, failCount);
    }

    @Transactional(readOnly = true)
    public PageResult<FileListItemVO> listMyFiles(String name, int pageNum, int pageSize) {
        User user = findCurrentUser();
        String nameFilter = StringUtils.hasText(name) ? name : null;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<DocFile> page = docFileRepository.findByUploaderAndName(user.getId(), nameFilter, pageable);
        return PageResult.from(page, FileListItemVO::from);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(Long id) {
        User user = findCurrentUser();
        DocFile file = docFileRepository.findByIdAndUploaderIdAndIsDeleted(id, user.getId(), 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        return buildDownloadResponse(file);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> preview(Long id) {
        User user = findCurrentUser();
        DocFile file = docFileRepository.findByIdAndUploaderIdAndIsDeleted(id, user.getId(), 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        if (file.getStatus() == null || file.getStatus() != STATUS_APPROVED) {
            throw new BusinessException(400, "文件未通过审核，无法预览");
        }
        if ("unsupported".equals(FileTypeUtils.resolvePreviewType(resolvePreviewKey(file)))) {
            throw new BusinessException(400, "该文件类型暂不支持在线预览");
        }

        return buildPreviewResponse(file);
    }

    @Transactional
    public void deleteFile(Long id) {
        User user = findCurrentUser();
        DocFile file = docFileRepository.findByIdAndUploaderIdAndIsDeleted(id, user.getId(), 0)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        file.setIsDeleted(1);
        file.setDeleteTime(LocalDateTime.now());
        docFileRepository.save(file);

        shareRecordRepository.findByFileIdAndStatus(id, 1).forEach(share -> {
            share.setStatus(0);
            shareRecordRepository.save(share);
        });
    }

    @Transactional(readOnly = true)
    public FileTypeStatisticsVO fileTypeStatistics() {
        User user = findCurrentUser();
        List<DocFile> files = docFileRepository.findByUploaderIdAndIsDeletedAndStatus(
                user.getId(), 0, STATUS_APPROVED);

        long document = 0;
        long image = 0;
        long video = 0;
        long compress = 0;

        for (DocFile file : files) {
            String category = FileTypeUtils.resolveCategory(file.getFormat());
            switch (category) {
                case "image" -> image++;
                case "video" -> video++;
                case "compress" -> compress++;
                default -> document++;
            }
        }

        return FileTypeStatisticsVO.builder()
                .document(document)
                .image(image)
                .video(video)
                .compress(compress)
                .build();
    }

    public ResponseEntity<Resource> buildDownloadResponse(DocFile file) {
        return buildDownloadResponse(file, false);
    }

    public ResponseEntity<Resource> buildDownloadResponse(DocFile file, boolean skipStatusCheck) {
        if (file.getIsDeleted() != null && file.getIsDeleted() == 1) {
            throw new BusinessException(404, "文件不存在");
        }
        if (!skipStatusCheck && (file.getStatus() == null || file.getStatus() != STATUS_APPROVED)) {
            throw new BusinessException(400, "文件未通过审核，无法下载");
        }

        Path path = fileStorageService.resolveStoredPath(file.getPath());
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new BusinessException(404, "文件不存在");
            }

            String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件下载失败");
        }
    }

    public ResponseEntity<Resource> buildPreviewResponse(DocFile file) {
        Path path = fileStorageService.resolveStoredPath(file.getPath());
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new BusinessException(404, "文件不存在");
            }

            String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            String contentType = FileTypeUtils.resolveContentType(resolvePreviewKey(file));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件预览失败");
        }
    }

    private String resolvePreviewKey(DocFile file) {
        if (StringUtils.hasText(file.getFormat())) {
            return file.getFormat();
        }
        return file.getName();
    }

    public DocFile getApprovedFile(Long fileId) {
        DocFile file = docFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        if (file.getIsDeleted() != null && file.getIsDeleted() == 1) {
            throw new BusinessException(404, "文件不存在");
        }
        if (file.getStatus() == null || file.getStatus() != STATUS_APPROVED) {
            throw new BusinessException(400, "文件未通过审核");
        }
        return file;
    }

    public Map<Long, DocFile> getFilesMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return docFileRepository.findAllById(ids).stream()
                .filter(f -> f.getIsDeleted() == null || f.getIsDeleted() == 0)
                .collect(Collectors.toMap(DocFile::getId, Function.identity()));
    }

    private String resolveDisplayName(String customName, String originalName, String format) {
        if (!StringUtils.hasText(customName)) {
            return originalName;
        }

        String name = customName.trim();
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new BusinessException("文件名包含非法字符");
        }
        if (!name.contains(".") && StringUtils.hasText(format)) {
            return name + format;
        }
        return name;
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
