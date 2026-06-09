package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.*;
import com.docmanage.entity.User;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import com.docmanage.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserProfileVO getProfile() {
        User user = findCurrentUser();
        return UserProfileVO.from(user);
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        User user = findCurrentUser();

        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getIdCard())) {
            user.setIdCard(request.getIdCard());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (StringUtils.hasText(request.getEducation())) {
            user.setEducation(request.getEducation());
        }
        if (StringUtils.hasText(request.getPhone())) {
            if (userRepository.existsByPhoneAndIdNot(request.getPhone(), user.getId())) {
                throw new BusinessException("手机号已被使用");
            }
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getIdentity())) {
            user.setIdentity(request.getIdentity());
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        User user = findCurrentUser();
        if (!PasswordUtils.matches(request.getOldPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        String newSalt = PasswordUtils.generateSalt();
        user.setSalt(newSalt);
        user.setPassword(PasswordUtils.encrypt(request.getNewPassword(), newSalt));
        userRepository.save(user);
    }

    @Transactional
    public AvatarUploadVO uploadAvatar(MultipartFile file) {
        User user = findCurrentUser();
        String avatarUrl = fileStorageService.storeAvatar(file);

        fileStorageService.deleteIfExists(user.getAvatar());
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return new AvatarUploadVO(avatarUrl);
    }

    @Transactional(readOnly = true)
    public UserDetailVO getUserById(Long id) {
        User currentUser = findCurrentUser();
        if (!currentUser.isAdmin()) {
            throw new BusinessException(403, "没有访问权限");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return UserDetailVO.from(user);
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
