package com.jianqing.framework.security.impl;

import com.jianqing.framework.security.JwtProperties;
import com.jianqing.framework.security.TokenSessionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
