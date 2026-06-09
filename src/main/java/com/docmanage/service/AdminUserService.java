package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.*;
import com.docmanage.entity.User;
import com.docmanage.repository.UserRepository;
import com.docmanage.service.support.AdminAuthSupport;
import com.docmanage.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final AdminAuthSupport adminAuth;

    @Transactional(readOnly = true)
    public PageResult<AdminUserListItemVO> listUsers(String username, String realName, String phone,
                                                     Integer status, int pageNum, int pageSize) {
        adminAuth.requireAdmin();
        return queryByRole("user", username, realName, phone, status, pageNum, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResult<AdminUserListItemVO> listAdmins(String username, String realName, String phone,
                                                      Integer status, int pageNum, int pageSize) {
        adminAuth.requireSuper();
        return queryByRole("admin", username, realName, phone, status, pageNum, pageSize);
    }

    @Transactional(readOnly = true)
    public UserDetailVO getUserDetail(Long id) {
        User operator = adminAuth.requireAdmin();
        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        adminAuth.canManageUser(operator, target);
        return UserDetailVO.from(target);
    }

    @Transactional
    public void updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User operator = adminAuth.requireAdmin();
        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        adminAuth.canManageUser(operator, target);
        adminAuth.assertNotSelf(operator, target, "禁用或启用");

        target.setStatus(request.getStatus());
        userRepository.save(target);
    }

    @Transactional
    public void resetPassword(Long id) {
        User operator = adminAuth.requireAdmin();
        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        adminAuth.canManageUser(operator, target);

        String salt = PasswordUtils.generateSalt();
        target.setSalt(salt);
        target.setPassword(PasswordUtils.encrypt(DEFAULT_PASSWORD, salt));
        userRepository.save(target);
    }

    @Transactional
    public void createAdmin(CreateAdminRequest request) {
        adminAuth.requireSuper();

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setRealName(request.getRealName());
        admin.setPhone(request.getPhone());
        admin.setRole("admin");
        admin.setStatus(1);

        String salt = PasswordUtils.generateSalt();
        admin.setSalt(salt);
        admin.setPassword(PasswordUtils.encrypt(request.getPassword(), salt));

        userRepository.save(admin);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        User operator = adminAuth.requireSuper();
        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "管理员不存在"));

        if (!"admin".equals(target.getRole())) {
            throw new BusinessException(400, "只能删除管理员账号");
        }
        adminAuth.assertNotSelf(operator, target, "删除");

        target.setStatus(0);
        userRepository.save(target);
    }

    private PageResult<AdminUserListItemVO> queryByRole(String role, String username, String realName,
                                                        String phone, Integer status,
                                                        int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<User> page = userRepository.findByRoleAndFilters(
                role,
                StringUtils.hasText(username) ? username : null,
                StringUtils.hasText(realName) ? realName : null,
                StringUtils.hasText(phone) ? phone : null,
                status,
                pageable
        );
        return PageResult.from(page, AdminUserListItemVO::from);
    }
}
