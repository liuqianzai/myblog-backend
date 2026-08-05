package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.vo.LoginVO;

/**
 * 认证与授权业务逻辑接口
 */
public interface AuthService {/**
 * login
 */

    LoginVO login(LoginDTO loginDTO);    /**
     * 校验令牌
     */


    Long verifyToken(String token);    /**
     * logout
     */


    void logout(String token);    /**
     * change密码
     */


    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
