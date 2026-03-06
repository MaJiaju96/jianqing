package com.jianqing.module.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.auth.dto.LoginRequest;
import com.jianqing.module.auth.dto.LoginResponse;
import com.jianqing.module.auth.dto.UserProfileResponse;
import com.jianqing.module.system.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口，负责登录、登出与当前用户身份信息查询。
 */
public interface AuthService extends IService<SysUser> {

    /**
     * 用户名密码登录。
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 退出登录并清理会话 token。
     */
    void logout(HttpServletRequest httpRequest);

    /**
     * 查询当前登录用户资料。
     */
    UserProfileResponse me();
}
