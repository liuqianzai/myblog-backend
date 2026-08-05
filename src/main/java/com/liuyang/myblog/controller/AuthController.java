package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.vo.LoginVO;
import com.liuyang.myblog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
/**
 * 认证与授权管理控制器
 */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    /**
     * login
     */

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return ApiResponse.ok(authService.login(loginDTO));
    }
    /**
     * logout
     */

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute(AuthContext.TOKEN_ATTRIBUTE);
        authService.logout(token);
        return ApiResponse.ok();
    }
    /**
     * change密码
     */

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthContext.USER_ID_ATTRIBUTE);
        authService.changePassword(userId, changePasswordDTO);
        return ApiResponse.ok();
    }
}
