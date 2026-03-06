package com.jianqing.framework.security;

public interface TokenSessionService {

    void saveToken(String token, String username);

    boolean isTokenActive(String token, String username);

    void removeToken(String token);
}
