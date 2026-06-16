package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.config.FileStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"
    );

    private final Path uploadRoot;
    private final long maxFileSize;

    public FileStorageService(FileStorageProperties properties) {
        this.maxFileSize = properties.getMaxSize() > 0 ? properties.getMaxSize() : 209715200L;
        this.uploadRoot = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new BusinessException(500, "无法创建上传目录");
        }
    }

    public String storeAvatar(MultipartFile file) {
        String relativePath = storeFile(file, "avatar", ALLOWED_CONTENT_TYPES, "仅支持上传图片文件");
        return "/uploads/" + relativePath;
    }

    public StoredFile storeDocument(MultipartFile file) {
        String relativePath = storeFile(file, "file", null, null);
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        return new StoredFile(relativePath, originalFilename, extension, file.getSize());
    }

    public Path resolveStoredPath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException(404, "文件不存在");
        }
        Path filePath = uploadRoot.resolve(relativePath).normalize();
        if (!filePath.startsWith(uploadRoot) || !Files.exists(filePath)) {
            throw new BusinessException(404, "文件不存在");
        }
        return filePath;
    }

    private String storeFile(MultipartFile file, String category, Set<String> allowedTypes, String typeErrorMsg) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过 " + formatSize(maxFileSize));
        }

        String contentType = file.getContentType();
        if (allowedTypes != null && (contentType == null || !allowedTypes.contains(contentType.toLowerCase()))) {
            throw new BusinessException(typeErrorMsg);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new BusinessException("文件名包含非法字符");
        }

        String extension = getExtension(originalFilename);
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        Path targetDir = uploadRoot.resolve(category).resolve(dateDir);
        Path targetFile = targetDir.resolve(storedFileName);

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return category + "/" + dateDir + "/" + storedFileName;
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }
    }

    public record StoredFile(String path, String originalName, String format, long size) {
    }

    public void deletePhysicalFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        Path filePath = uploadRoot.resolve(relativePath).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // 物理删除失败不影响主流程
        }
    }

    public void deleteIfExists(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl) || !avatarUrl.startsWith("/uploads/")) {
            return;
        }
        Path filePath = uploadRoot.resolve(avatarUrl.substring("/uploads/".length()));
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // 删除旧头像失败不影响主流程
        }
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return (bytes / (1024 * 1024)) + "MB";
        }
        return (bytes / 1024) + "KB";
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return filename.substring(dotIndex).toLowerCase();
        }
        return ".jpg";
    }
}
