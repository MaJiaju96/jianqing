package com.jianqing.framework.security;

import com.jianqing.framework.security.impl.TokenSessionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void shouldSaveTokenWithExpectedKeyAndTtl() {
        JwtProperties jwtProperties = buildJwtProperties();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        TokenSessionService tokenSessionService = new TokenSessionServiceImpl(stringRedisTemplate, jwtProperties);

        tokenSessionService.saveToken("token-1", "admin");

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq("jq:token:token-1"),
                org.mockito.ArgumentMatchers.eq("admin"),
                durationCaptor.capture());
        Assertions.assertEquals(Duration.ofSeconds(7200), durationCaptor.getValue());
    }

    @Test
    void shouldReturnTrueWhenTokenMatchesUsername() {
        JwtProperties jwtProperties = buildJwtProperties();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("jq:token:token-2")).thenReturn("alice");
        TokenSessionService tokenSessionService = new TokenSessionServiceImpl(stringRedisTemplate, jwtProperties);

        boolean active = tokenSessionService.isTokenActive("token-2", "alice");

        Assertions.assertTrue(active);
    }

    @Test
    void shouldReturnFalseWhenTokenDoesNotMatchUsername() {
        JwtProperties jwtProperties = buildJwtProperties();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("jq:token:token-3")).thenReturn("bob");
        TokenSessionService tokenSessionService = new TokenSessionServiceImpl(stringRedisTemplate, jwtProperties);

        boolean active = tokenSessionService.isTokenActive("token-3", "alice");

        Assertions.assertFalse(active);
    }

    @Test
    void shouldDeleteTokenByKeyOnLogout() {
        JwtProperties jwtProperties = buildJwtProperties();
        TokenSessionService tokenSessionService = new TokenSessionServiceImpl(stringRedisTemplate, jwtProperties);

        tokenSessionService.removeToken("token-4");

        verify(stringRedisTemplate).delete("jq:token:token-4");
    }

    private JwtProperties buildJwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("change-this-secret-at-least-32-bytes-long");
        jwtProperties.setExpireSeconds(7200L);
        jwtProperties.setTokenKeyPrefix("jq:token:");
        return jwtProperties;
    }
}
