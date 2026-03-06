package com.jianqing.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.framework.security.JwtProperties;
import com.jianqing.framework.security.JwtTokenService;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.framework.security.TokenSessionService;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.service.AuditLogService;
import com.jianqing.module.auth.dto.LoginRequest;
import com.jianqing.module.auth.dto.LoginResponse;
import com.jianqing.module.auth.dto.UserProfileResponse;
import com.jianqing.module.auth.service.AuthService;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.SystemService;
import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务：登录、登出、获取用户信息
 * 登录成功后写入 Redis 会话，支持服务端主动失效
 * 登录日志统一通过 AuditLogService 写入，避免认证层直接依赖持久层 Mapper
 */
@Service
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AuthService {

    private final SystemService systemService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final TokenSessionService tokenSessionService;

    public AuthServiceImpl(SystemService systemService,
                           AuditLogService auditLogService,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           JwtProperties jwtProperties,
                           TokenSessionService tokenSessionService) {
        this.systemService = systemService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.tokenSessionService = tokenSessionService;
    }

    /**
     * 用户登录
     * @param request 登录请求（username/password）
     * @param httpRequest 用于获取客户端 IP
     * @return 登录响应（token/Bearer/过期时间）
     * @throws IllegalArgumentException 账号不存在、禁用或密码错误
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 查询用户：username 唯一 + 未删除
        SysUser user = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));

        // 校验用户状态：存在 + 启用(1) + 密码匹配
        // 统一返回「用户名或密码错误」，防止账号枚举攻击
        if (user == null || user.getStatus() == null || user.getStatus() != 1
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveLoginLog(0L, request.getUsername(), httpRequest, 0, "用户名或密码错误");
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 更新最后登录 IP 和时间
        user.setLastLoginIp(clientIp(httpRequest));
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);

        // 生成 JWT 并写入 Redis 会话（支持服务端主动失效）
        String token = jwtTokenService.generateToken(user.getUsername());
        tokenSessionService.saveToken(token, user.getUsername());
        saveLoginLog(user.getId(), user.getUsername(), httpRequest, 1, "登录成功");
        return new LoginResponse(token, "Bearer", jwtProperties.getExpireSeconds());
    }

    /**
     * 用户登出：清除 Redis 会话
     * @param httpRequest 用于提取 Bearer Token
     */
    @Override
    public void logout(HttpServletRequest httpRequest) {
        String token = resolveBearerToken(httpRequest);
        if (token == null) {
            return;
        }
        tokenSessionService.removeToken(token);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户profile（含角色、权限）
     * @throws IllegalArgumentException 未登录或用户不存在
     */
    @Override
    public UserProfileResponse me() {
        String username = SecurityUtils.currentUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("未登录或 token 无效");
        }
        SysUser user = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<String> roles = systemService.listRoleCodesByUserId(user.getId());
        List<String> perms = systemService.listPermissionsByUserId(user.getId());
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getNickname(), roles, perms);
    }

    private void saveLoginLog(Long userId,
                              String username,
                              HttpServletRequest request,
                              int status,
                              String message) {
        // 登录日志统一通过审计服务写入，避免认证层直接依赖持久层 Mapper。
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId == null ? 0L : userId);
        log.setUsername(username == null ? "" : username);
        log.setLoginType("password");
        log.setLoginIp(clientIp(request));
        log.setLoginLocation("");
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setOs("");
        log.setBrowser("");
        log.setStatus(status);
        log.setMsg(message);
        auditLogService.saveLoginLog(log);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
