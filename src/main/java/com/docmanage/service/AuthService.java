package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.LoginRequest;
import com.docmanage.dto.LoginResponse;
import com.docmanage.dto.RegisterRequest;
import com.docmanage.entity.User;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.JwtTokenProvider;
import com.docmanage.security.LoginUser;
import com.docmanage.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (user.getStatus() != 1) {
            throw new BusinessException(401, "账号已被禁用");
        }

        if (!PasswordUtils.matches(request.getPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        LoginUser loginUser = new LoginUser(user);
        String token = jwtTokenProvider.generateToken(loginUser);
        String displayName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        return new LoginResponse(token, user.getRole(), displayName);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已被注册");
        }

        String salt = PasswordUtils.generateSalt();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encrypt(request.getPassword(), salt));
        user.setSalt(salt);
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setIdCard(StringUtils.hasText(request.getIdCard()) ? request.getIdCard() : null);
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setBirthday(request.getBirthday());
        user.setEducation(StringUtils.hasText(request.getEducation()) ? request.getEducation() : null);
        user.setIdentity(StringUtils.hasText(request.getIdentity()) ? request.getIdentity() : null);
        user.setRole("user");
        user.setStatus(1);
        userRepository.save(user);
    }
}
