package com.jianqing.framework.security;

/**
 * JWT 服务接口，负责 token 生成与解析。
 */
public interface JwtTokenService {

    /**
     * 生成指定用户名的 JWT token。
     */
    String generateToken(String username);

    /**
     * 从 JWT token 解析用户名。
     */
    String parseUsername(String token);
}
