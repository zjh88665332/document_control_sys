package com.docmanage.service.support;

import com.docmanage.common.BusinessException;
import com.docmanage.entity.User;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthSupport {

    private final UserRepository userRepository;

    public User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }

    public User requireAdmin() {
        User user = findCurrentUser();
        if (!user.isAdmin()) {
            throw new BusinessException(403, "无权限");
        }
        return user;
    }

    public User requireSuper() {
        User user = findCurrentUser();
        if (!"super".equals(user.getRole())) {
            throw new BusinessException(403, "需要超级管理员权限");
        }
        return user;
    }

    public void canManageUser(User operator, User target) {
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("super".equals(target.getRole())) {
            throw new BusinessException(403, "无权管理超级管理员");
        }
        if ("admin".equals(target.getRole()) && !"super".equals(operator.getRole())) {
            throw new BusinessException(403, "仅超级管理员可管理管理员账号");
        }
        if ("user".equals(target.getRole()) && !operator.isAdmin()) {
            throw new BusinessException(403, "无权限");
        }
    }

    public void assertNotSelf(User operator, User target, String action) {
        if (operator.getId().equals(target.getId())) {
            throw new BusinessException(400, "不能" + action + "自己");
        }
    }
}
