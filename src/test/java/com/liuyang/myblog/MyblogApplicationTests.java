package com.liuyang.myblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyblogApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void encodePwd() {
        // 你想登录用的明文密码
        String rawPassword = "admin123";

        // 用你项目的加密器自动生成正确密码
        String encode = passwordEncoder.encode(rawPassword);

        // 控制台会输出 真正能用的加密密码
        System.out.println("正确加密密码：" + encode);
    }
}
