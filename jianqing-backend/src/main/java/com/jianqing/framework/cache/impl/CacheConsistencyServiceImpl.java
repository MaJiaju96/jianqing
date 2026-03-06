package com.jianqing.framework.cache.impl;

import com.jianqing.framework.cache.CacheConsistencyService;
import com.jianqing.framework.cache.CacheProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CacheConsistencyServiceImpl implements CacheConsistencyService {

    private final CacheManager cacheManager;
    private final CacheProperties cacheProperties;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public CacheConsistencyServiceImpl(CacheManager cacheManager,
                                       CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public void deleteWithDelay(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        evict(cacheName, key);
        scheduledExecutorService.schedule(() -> evict(cacheName, key),
                cacheProperties.getDelayDoubleDeleteMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void deleteWithDelay(String cacheName, Collection<?> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (Object key : keys) {
            deleteWithDelay(cacheName, key);
        }
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    @PreDestroy
    public void destroy() {
        scheduledExecutorService.shutdown();
    }
}
