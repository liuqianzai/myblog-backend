package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.po.BlogUser;
import com.liuyang.myblog.domain.vo.LoginVO;
import com.liuyang.myblog.mapper.BlogUserMapper;
import com.liuyang.myblog.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
/**
 * 认证与授权业务逻辑接口实现类
 */
public class AuthServiceImpl implements AuthService {
    private final BlogUserMapper blogUserMapper;
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    @Value("${blog.auth.password-salt:myblog-default-salt}")
    private String passwordSalt;

    public AuthServiceImpl(BlogUserMapper blogUserMapper) {
        this.blogUserMapper = blogUserMapper;
    }
    /**
     * login
     */

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw BusinessException.badRequest("username and password are required");
        }

        BlogUser user = blogUserMapper.selectOne(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getUsername, loginDTO.getUsername())
                .eq(BlogUser::getStatus,1)
                .last("limit 1"));
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw BusinessException.unauthorized("invalid username or password");
        }

        String inputHash = sha256(loginDTO.getPassword() + passwordSalt);
        if (!inputHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw BusinessException.unauthorized("invalid username or password");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());
        return new LoginVO(token, user.getUsername(), user.getNickname());
    }
    /**
     * 校验令牌
     */

    @Override
    public Long verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return tokenStore.get(token);
    }
    /**
     * logout
     */

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            tokenStore.remove(token);
        }
    }
    /**
     * change密码
     */

    @Override
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        if (userId == null) {
            throw BusinessException.unauthorized("unauthorized");
        }
        if (changePasswordDTO == null
                || !StringUtils.hasText(changePasswordDTO.getOldPassword())
                || !StringUtils.hasText(changePasswordDTO.getNewPassword())) {
            throw BusinessException.badRequest("old password and new password are required");
        }
        if (changePasswordDTO.getNewPassword().length() < 6) {
            throw BusinessException.badRequest("new password length must be at least 6");
        }

        BlogUser user = blogUserMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("unauthorized");
        }
        String oldPasswordHash = sha256(changePasswordDTO.getOldPassword() + passwordSalt);
        if (!oldPasswordHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw BusinessException.unauthorized("old password is incorrect");
        }
        user.setPasswordHash(sha256(changePasswordDTO.getNewPassword() + passwordSalt));
        blogUserMapper.updateById(user);
    }    /**
     * sha256
     */


    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
