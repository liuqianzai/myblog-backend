package com.liuyang.myblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@SpringBootTest
class MyblogApplicationTests {

    @Value("${blog.auth.password-salt}")
    private String passwordSalt;

    @Test
    void contextLoads() {
    }

    @Test
    void encodePwd() {
        String rawPassword = "admin123";
        String encode = sha256(rawPassword + passwordSalt);
        System.out.println("正确加密密码：" + encode);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
