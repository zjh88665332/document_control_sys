package com.docmanage.util;

import org.springframework.util.StringUtils;

import java.util.Set;

public final class FileTypeUtils {

    private static final Set<String> DOCUMENT = Set.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv", "rtf"
    );
    private static final Set<String> IMAGE = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );
    private static final Set<String> VIDEO = Set.of(
            "mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"
    );
    private static final Set<String> COMPRESS = Set.of(
            "zip", "rar", "7z", "tar", "gz", "bz2"
    );

    private FileTypeUtils() {
    }

    public static String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static String resolveCategory(String extension) {
        if (!StringUtils.hasText(extension)) {
            return "document";
        }
        String ext = extension.toLowerCase();
        if (DOCUMENT.contains(ext)) {
            return "document";
        }
        if (IMAGE.contains(ext)) {
            return "image";
        }
        if (VIDEO.contains(ext)) {
            return "video";
        }
        if (COMPRESS.contains(ext)) {
            return "compress";
        }
        return "document";
    }

    public static String resolveDisplayType(String category) {
        return switch (category) {
            case "image" -> "图片";
            case "video" -> "视频";
            case "compress" -> "压缩包";
            default -> "文档";
        };
    }

    public static String normalizeExtension(String formatOrName) {
        if (!StringUtils.hasText(formatOrName)) {
            return "";
        }
        String value = formatOrName.trim().toLowerCase();
        if (value.startsWith(".")) {
            value = value.substring(1);
        }
        if (value.contains(".")) {
            return getExtension(value);
        }
        return value;
    }

    public static String resolvePreviewType(String formatOrName) {
        String ext = normalizeExtension(formatOrName);
        if (IMAGE.contains(ext)) {
            return "image";
        }
        if ("pdf".equals(ext)) {
            return "pdf";
        }
        if (VIDEO.contains(ext)) {
            return "video";
        }
        if (Set.of("txt", "csv", "md", "json", "xml", "html", "htm", "css", "js", "log").contains(ext)) {
            return "text";
        }
        return "unsupported";
    }

    public static String resolveContentType(String formatOrName) {
        String ext = normalizeExtension(formatOrName);
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "txt", "csv", "md", "log" -> "text/plain; charset=UTF-8";
            case "html", "htm" -> "text/html; charset=UTF-8";
            case "css" -> "text/css; charset=UTF-8";
            case "js" -> "text/javascript; charset=UTF-8";
            case "json" -> "application/json; charset=UTF-8";
            case "xml" -> "application/xml; charset=UTF-8";
            default -> "application/octet-stream";
        };
    }
}
