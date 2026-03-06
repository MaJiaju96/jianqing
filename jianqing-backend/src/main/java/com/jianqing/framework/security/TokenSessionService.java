package com.jianqing.framework.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public TokenSessionService(StringRedisTemplate stringRedisTemplate,
                               JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    public void saveToken(String token, String username) {
        String key = tokenKey(token);
        stringRedisTemplate.opsForValue().set(key, username, Duration.ofSeconds(jwtProperties.getExpireSeconds()));
    }

    public boolean isTokenActive(String token, String username) {
        String cacheUsername = stringRedisTemplate.opsForValue().get(tokenKey(token));
        return username != null && username.equals(cacheUsername);
    }

    public void removeToken(String token) {
        stringRedisTemplate.delete(tokenKey(token));
    }

    private String tokenKey(String token) {
        return jwtProperties.getTokenKeyPrefix() + token;
    }
}
