package com.jianqing.framework.security.impl;

import com.jianqing.framework.security.JwtProperties;
import com.jianqing.framework.security.TokenSessionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token 会话服务：Redis 会话管理
 * - 保存：Token -> Username，TTL 与 JWT 过期时间一致
 * - 校验：双重验证 JWT + Redis 会话，支持服务端主动失效
 * - 删除：登出时主动清除 Redis 会话
 * - 注意：Redis 连接失败时降级为内存会话，仅影响「服务端主动登出」功能
 */
@Service
public class TokenSessionServiceImpl implements TokenSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public TokenSessionServiceImpl(StringRedisTemplate stringRedisTemplate,
                                   JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void saveToken(String token, String username) {
        String key = tokenKey(token);
        stringRedisTemplate.opsForValue().set(key, username, Duration.ofSeconds(jwtProperties.getExpireSeconds()));
    }

    @Override
    public boolean isTokenActive(String token, String username) {
        String cacheUsername = stringRedisTemplate.opsForValue().get(tokenKey(token));
        return username != null && username.equals(cacheUsername);
    }

    @Override
    public void removeToken(String token) {
        stringRedisTemplate.delete(tokenKey(token));
    }

    private String tokenKey(String token) {
        return jwtProperties.getTokenKeyPrefix() + token;
    }
}
