package com.liuyang.myblog.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类（基于 JDK 原生 HMAC-SHA256 与 Jackson，无外部库依赖）
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long expireSeconds;
    private final ObjectMapper objectMapper;

    /**
     * 签名密钥最短长度。HS256 的密钥短于 256 位时会显著降低暴力破解成本。
     */
    private static final int MIN_SECRET_LENGTH = 32;

    public JwtUtil(
            // 此处刻意不提供默认值：一旦有兜底默认值，即使配置项被误删，
            // 应用仍会用这个公开的字符串签发令牌，等同于把签名密钥写进了仓库。
            // 缺失配置时构造失败、启动中断，才能暴露问题。
            @Value("${blog.auth.jwt-secret}") String secret,
            @Value("${blog.auth.jwt-expire-seconds:604800}") long expireSeconds,
            ObjectMapper objectMapper) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("blog.auth.jwt-secret 未配置，请在环境变量 BLOG_AUTH_JWT_SECRET 或 local 配置文件中设置");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("blog.auth.jwt-secret 长度不足 " + MIN_SECRET_LENGTH + " 位，请更换为随机生成的长密钥");
        }
        this.secret = secret;
        this.expireSeconds = expireSeconds;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成 JWT 令牌
     *
     * @param userId 用户ID
     * @return 包含签名的标准 JWT Token
     */
    public String generateToken(Long userId) {
        try {
            long now = System.currentTimeMillis() / 1000;
            long exp = now + expireSeconds;

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("iat", now);
            payload.put("exp", exp);

            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));

            String content = encodedHeader + "." + encodedPayload;
            String signature = sign(content, secret);

            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate JWT token", e);
        }
    }

    /**
     * 校验 JWT 令牌并提取 userId
     *
     * @param token 客户端传入的 Token
     * @return 合法且未过期返回用户ID，否则返回 null
     */
    public Long verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String signature = parts[2];

        String content = encodedHeader + "." + encodedPayload;
        String expectedSignature = sign(content, secret);

        // 使用恒定时间比较，防止时序侧信道攻击
        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(encodedPayload);
            JsonNode jsonNode = objectMapper.readTree(payloadBytes);
            if (!jsonNode.has("exp") || !jsonNode.has("sub")) {
                return null;
            }
            long exp = jsonNode.get("exp").asLong();
            long now = System.currentTimeMillis() / 1000;
            if (now > exp) {
                return null; // 已过期
            }
            return Long.parseLong(jsonNode.get("sub").asText());
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign HMAC-SHA256", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
