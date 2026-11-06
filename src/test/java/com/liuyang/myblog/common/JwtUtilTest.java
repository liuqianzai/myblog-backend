package com.liuyang.myblog.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT 令牌工具单元测试
 *
 * 覆盖：正常签发与解析、篡改令牌、密钥缺失或过短时的快速失败。
 */
class JwtUtilTest {

    private static final String VALID_SECRET = "0123456789abcdef0123456789abcdef";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("正常签发后能解析出同一个用户ID")
    void shouldRoundTripToken() {
        JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 3600, objectMapper);

        String token = jwtUtil.generateToken(42L);

        assertNotNull(token);
        assertEquals(2, token.split("\\.").length - 1);
        assertEquals(42L, jwtUtil.verifyToken(token));
    }

    @Test
    @DisplayName("篡改payload后校验失败")
    void shouldRejectTamperedToken() {
        JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 3600, objectMapper);
        String token = jwtUtil.generateToken(42L);

        String[] parts = token.split("\\.");
        // 换掉签名段的最后一个字符，模拟客户端伪造用户ID
        char lastChar = parts[2].charAt(parts[2].length() - 1);
        String tamperedSignature = parts[2].substring(0, parts[2].length() - 1) + (lastChar == 'A' ? 'B' : 'A');

        assertNull(jwtUtil.verifyToken(parts[0] + "." + parts[1] + "." + tamperedSignature));
    }

    @Test
    @DisplayName("过期令牌校验失败")
    void shouldRejectExpiredToken() {
        // 有效期设为 -1 秒，签发即过期
        JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, -1, objectMapper);

        assertNull(jwtUtil.verifyToken(jwtUtil.generateToken(1L)));
    }

    @Test
    @DisplayName("空令牌与格式非法令牌返回null而不抛异常")
    void shouldReturnNullForMalformedToken() {
        JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 3600, objectMapper);

        assertNull(jwtUtil.verifyToken(null));
        assertNull(jwtUtil.verifyToken(""));
        assertNull(jwtUtil.verifyToken("not-a-jwt"));
        assertNull(jwtUtil.verifyToken("a.b.c"));
    }

    @Test
    @DisplayName("密钥为空时启动即失败，不允许退化成公开默认密钥")
    void shouldFailFastWhenSecretBlank() {
        IllegalStateException blank = assertThrows(IllegalStateException.class,
                () -> new JwtUtil(" ", 3600, objectMapper));
        assertTrue(blank.getMessage().contains("jwt-secret"));
    }

    @Test
    @DisplayName("密钥过短时启动即失败")
    void shouldFailFastWhenSecretTooShort() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("short-secret", 3600, objectMapper));
    }
}
