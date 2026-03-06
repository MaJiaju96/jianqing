package com.jianqing.framework.security;

/**
 * token 会话服务接口，负责 token 活跃态存储与失效。
 */
public interface TokenSessionService {

    /**
     * 保存 token 会话并设置过期时间。
     */
    void saveToken(String token, String username);

    /**
     * 校验 token 是否处于活跃状态。
     */
    boolean isTokenActive(String token, String username);

    /**
     * 删除 token 会话。
     */
    void removeToken(String token);
}
