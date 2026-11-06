package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.common.JwtUtil;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.po.BlogUser;
import com.liuyang.myblog.domain.vo.LoginVO;
import com.liuyang.myblog.mapper.BlogUserMapper;
import com.liuyang.myblog.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
/**
 * 认证与授权业务逻辑接口实现类
 */
public class AuthServiceImpl implements AuthService {
    private final BlogUserMapper blogUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 旧版「全局固定盐 + 单次 SHA-256」的盐值，仅用于兼容历史密码并触发自动升级，
    // 新密码一律使用 BCrypt，不再读取该配置。
    @Value("${blog.auth.password-salt:}")
    private String legacyPasswordSalt;

    public AuthServiceImpl(BlogUserMapper blogUserMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.blogUserMapper = blogUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
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
                .eq(BlogUser::getStatus, 1)
                .last("limit 1"));
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw BusinessException.unauthorized("invalid username or password");
        }

        if (!matchesAndUpgrade(loginDTO.getPassword(), user)) {
            throw BusinessException.unauthorized("invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new LoginVO(token, user.getUsername(), user.getNickname());
    }

    /**
     * 校验令牌
     */
    @Override
    public Long verifyToken(String token) {
        return jwtUtil.verifyToken(token);
    }

    /**
     * logout
     */
    @Override
    public void logout(String token) {
        // JWT 为无状态令牌，登出由前端清除客户端缓存即可
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
        if (!matchesAndUpgrade(changePasswordDTO.getOldPassword(), user)) {
            throw BusinessException.unauthorized("old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        blogUserMapper.updateById(user);
    }

    /**
     * 校验密码，并在命中旧版 SHA-256 哈希时顺带升级为 BCrypt。
     *
     * 这样库中已有的账号可以继续登录，登录成功后自动换成 BCrypt 哈希，
     * 不需要停机改密码，也不会把用户锁在门外。
     */
    private boolean matchesAndUpgrade(String rawPassword, BlogUser user) {
        String storedHash = user.getPasswordHash();
        if (!StringUtils.hasText(storedHash)) {
            return false;
        }

        if (isBcryptHash(storedHash)) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }

        // ---- 以下为旧版 SHA-256 兼容逻辑，命中后立即升级 ----
        if (!StringUtils.hasText(legacyPasswordSalt)) {
            log.warn("账号 {} 使用旧版密码哈希，但 blog.auth.password-salt 未配置，无法完成校验", user.getUsername());
            return false;
        }
        String legacyHash = sha256(rawPassword + legacyPasswordSalt);
        // 恒定时间比较，避免逐字节比较带来的时序侧信道
        boolean matched = MessageDigest.isEqual(
                legacyHash.getBytes(StandardCharsets.UTF_8),
                storedHash.toLowerCase().getBytes(StandardCharsets.UTF_8));
        if (!matched) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        blogUserMapper.updateById(user);
        log.info("账号 {} 的密码哈希已从旧版 SHA-256 自动升级为 BCrypt", user.getUsername());
        return true;
    }

    private boolean isBcryptHash(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    /**
     * sha256（仅用于兼容历史密码）
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
