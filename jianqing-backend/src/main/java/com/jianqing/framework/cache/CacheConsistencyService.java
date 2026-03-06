package com.jianqing.framework.cache;

import java.util.Collection;

/**
 * 缓存一致性服务接口，负责缓存删除与延迟双删执行。
 */
public interface CacheConsistencyService {

    /**
     * 对单个缓存键执行删除与延迟双删。
     */
    void deleteWithDelay(String cacheName, Object key);

    /**
     * 对多个缓存键执行删除与延迟双删。
     */
    void deleteWithDelay(String cacheName, Collection<?> keys);
}
