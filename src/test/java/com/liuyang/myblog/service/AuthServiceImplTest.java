package com.liuyang.myblog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.common.JwtUtil;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.po.BlogUser;
import com.liuyang.myblog.domain.vo.LoginVO;
import com.liuyang.myblog.mapper.BlogUserMapper;
import com.liuyang.myblog.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final String LEGACY_SALT = "legacy-test-salt";

    @Mock
    private BlogUserMapper blogUserMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // 测试用低强度，保证速度

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 3600, new ObjectMapper());
        authService = new AuthServiceImpl(blogUserMapper, jwtUtil, passwordEncoder);
        ReflectionTestUtils.setField(authService, "legacyPasswordSalt", LEGACY_SALT);
    }

    @Test
    @DisplayName("BCrypt 哈希的账号可以正常登录")
    void testLoginWithBcryptHash() {
        BlogUser user = buildUser(passwordEncoder.encode("correct-password"));
        when(blogUserMapper.selectOne(any())).thenReturn(user);

        LoginVO loginVO = authService.login(login("correct-password"));

        assertNotNull(loginVO.getToken());
        assertEquals("admin", loginVO.getUsername());
        verify(blogUserMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("密码错误时拒绝登录")
    void testLoginWithWrongPassword() {
        BlogUser user = buildUser(passwordEncoder.encode("correct-password"));
        when(blogUserMapper.selectOne(any())).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(login("wrong-password")));
        assertEquals(401, ex.getCode());
    }

    @Test
    @DisplayName("旧版 SHA-256 哈希登录成功后自动升级为 BCrypt")
    void testLegacyHashUpgradedToBcrypt() {
        BlogUser user = buildUser(sha256("old-password" + LEGACY_SALT));
        when(blogUserMapper.selectOne(any())).thenReturn(user);

        LoginVO loginVO = authService.login(login("old-password"));

        assertNotNull(loginVO.getToken());
        // 登录成功后应把 BCrypt 哈希写回数据库，以后不再依赖旧算法
        assertTrue(user.getPasswordHash().startsWith("$2"), "哈希应已升级为 BCrypt");
        assertTrue(passwordEncoder.matches("old-password", user.getPasswordHash()));
        verify(blogUserMapper, times(1)).updateById(user);
    }

    @Test
    @DisplayName("旧版哈希密码错误时不升级、不写库")
    void testLegacyHashWrongPasswordNotUpgraded() {
        BlogUser user = buildUser(sha256("old-password" + LEGACY_SALT));
        when(blogUserMapper.selectOne(any())).thenReturn(user);

        assertThrows(BusinessException.class, () -> authService.login(login("bad-password")));

        assertFalse(user.getPasswordHash().startsWith("$2"));
        verify(blogUserMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("账号被停用时拒绝登录")
    void testLoginRejectsDisabledUser() {
        // 查询条件里已带 status = 1，停用账号查不出来
        when(blogUserMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(login("any-password")));
        assertEquals(401, ex.getCode());
    }

    @Test
    @DisplayName("修改密码时校验旧密码并把新密码存为 BCrypt")
    void testChangePasswordUsesBcrypt() {
        BlogUser user = buildUser(sha256("old-password" + LEGACY_SALT));
        user.setId(1L);
        when(blogUserMapper.selectById(1L)).thenReturn(user);

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("old-password");
        dto.setNewPassword("brand-new-password");

        authService.changePassword(1L, dto);

        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches("brand-new-password", user.getPasswordHash()));
        assertFalse(passwordEncoder.matches("old-password", user.getPasswordHash()));
    }

    @Test
    @DisplayName("修改密码时旧密码错误则拒绝")
    void testChangePasswordRejectsWrongOldPassword() {
        BlogUser user = buildUser(passwordEncoder.encode("old-password"));
        user.setId(1L);
        when(blogUserMapper.selectById(1L)).thenReturn(user);

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("not-the-old-one");
        dto.setNewPassword("brand-new-password");

        assertThrows(BusinessException.class, () -> authService.changePassword(1L, dto));
        assertTrue(passwordEncoder.matches("old-password", user.getPasswordHash()));
    }

    private BlogUser buildUser(String passwordHash) {
        BlogUser user = new BlogUser();
        user.setId(2L);
        user.setUsername("admin");
        user.setNickname("Liu Yang");
        user.setRole("admin");
        user.setStatus(1);
        user.setPasswordHash(passwordHash);
        return user;
    }

    private LoginDTO login(String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword(password);
        return dto;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
