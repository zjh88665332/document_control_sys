package com.docmanage.util;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class FileTagUtils {

    private FileTagUtils() {
    }

    public static String generateTags(String name, String format, String remark) {
        Set<String> tags = new LinkedHashSet<>();
        String category = FileTypeUtils.resolveCategory(format);
        tags.add(FileTypeUtils.resolveDisplayType(category));

        String ext = FileTypeUtils.normalizeExtension(format);
        if (StringUtils.hasText(ext)) {
            tags.add(ext);
        }

        if (StringUtils.hasText(name)) {
            String[] parts = name.replaceAll("[._\\-]", " ").split("\\s+");
            for (String part : parts) {
                if (part.length() >= 2 && !part.matches("\\d+")) {
                    tags.add(part);
                }
            }
        }

        if (StringUtils.hasText(remark)) {
            tags.add(remark.trim());
        }

        return String.join(",", tags);
    }
}
