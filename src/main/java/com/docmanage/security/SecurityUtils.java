package com.docmanage.security;

import com.docmanage.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(401, "未授权");
        }
        return loginUser;
    }

    public static Long getCurrentUserId() {
        return getLoginUser().getUserId();
    }

    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentUsernameOrNull() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            return null;
        }
    }
}
