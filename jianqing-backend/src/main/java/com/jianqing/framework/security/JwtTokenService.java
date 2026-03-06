package com.jianqing.framework.security;

public interface JwtTokenService {

    String generateToken(String username);

    String parseUsername(String token);
}
