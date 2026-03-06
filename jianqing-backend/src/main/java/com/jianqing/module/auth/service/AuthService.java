package com.jianqing.module.auth.service;

import com.jianqing.module.auth.dto.LoginRequest;
import com.jianqing.module.auth.dto.LoginResponse;
import com.jianqing.module.auth.dto.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout(HttpServletRequest httpRequest);

    UserProfileResponse me();
}
