package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.LoginDTO;
import com.liuyang.myblog.domain.dto.ChangePasswordDTO;
import com.liuyang.myblog.domain.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);

    Long verifyToken(String token);

    void logout(String token);

    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
