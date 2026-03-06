package com.jianqing.framework.security;

import com.jianqing.framework.security.impl.JwtTokenServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

class JwtTokenServiceTest {

    @Test
    void shouldGenerateTokenAndParseUsername() {
        JwtProperties jwtProperties = buildJwtProperties(3600L);
        JwtTokenService jwtTokenService = new JwtTokenServiceImpl(jwtProperties);

        String token = jwtTokenService.generateToken("admin");

        String username = jwtTokenService.parseUsername(token);
        Assertions.assertEquals("admin", username);
    }

    @Test
    void shouldApplyExpireSecondsToTokenExpiration() {
        JwtProperties jwtProperties = buildJwtProperties(120L);
        JwtTokenService jwtTokenService = new JwtTokenServiceImpl(jwtProperties);

        String token = jwtTokenService.generateToken("tester");
        Claims claims = Jwts.parser()
                .verifyWith(secretKey(jwtProperties.getSecret()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Instant now = Instant.now();
        Assertions.assertTrue(claims.getExpiration().toInstant().isAfter(now.plusSeconds(60)));
        Assertions.assertTrue(claims.getExpiration().toInstant().isBefore(now.plusSeconds(180)));
    }

    @Test
    void shouldThrowWhenSecretTooShort() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("short-secret");
        jwtProperties.setExpireSeconds(60L);
        JwtTokenService jwtTokenService = new JwtTokenServiceImpl(jwtProperties);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.generateToken("admin"));
        Assertions.assertTrue(exception.getMessage().contains("至少需要 32 个字符"));
    }

    private JwtProperties buildJwtProperties(long expireSeconds) {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("change-this-secret-at-least-32-bytes-long");
        jwtProperties.setExpireSeconds(expireSeconds);
        jwtProperties.setTokenKeyPrefix("jq:token:");
        return jwtProperties;
    }

    private SecretKey secretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
