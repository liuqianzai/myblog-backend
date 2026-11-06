package com.liuyang.myblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码哈希配置
 *
 * 使用 BCrypt 代替原来的「全局固定盐 + 单次 SHA-256」：
 * BCrypt 自带随机盐、可调工作因子，且计算慢，能显著提高离线爆破成本。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 10 为默认值，单次校验约 50~100ms，兼顾安全与登录体验
        return new BCryptPasswordEncoder(10);
    }
}
