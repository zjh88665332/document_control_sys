package com.docmanage.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static String encrypt(String rawPassword, String salt) {
        return DigestUtils.md5DigestAsHex((rawPassword + salt).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean matches(String rawPassword, String salt, String encryptedPassword) {
        return encrypt(rawPassword, salt).equals(encryptedPassword);
    }
}
