package com.docmanage.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class TextExtractUtils {

    private static final int MAX_LENGTH = 2000;
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "txt", "md", "csv", "json", "xml", "html", "htm", "log"
    );

    private TextExtractUtils() {
    }

    public static String extractSearchContent(MultipartFile file, String format) {
        String ext = FileTypeUtils.normalizeExtension(format);
        if (!TEXT_EXTENSIONS.contains(ext)) {
            return null;
        }
        try {
            byte[] bytes = file.getBytes();
            int len = Math.min(bytes.length, MAX_LENGTH);
            String content = new String(bytes, 0, len, StandardCharsets.UTF_8);
            return StringUtils.hasText(content) ? content.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
