package com.jianqing.module.system.service.impl;

import com.jianqing.framework.cache.CacheConsistencyService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
public class SystemCacheEvictor {

    private static final String CACHE_SYSTEM_USERS = "system:users";
    private static final String CACHE_SYSTEM_ROLES = "system:roles";
    private static final String CACHE_SYSTEM_MENUS = "system:menus";
    private static final String CACHE_USER_MENU_TREE = "user:menu-tree";
    private static final String CACHE_USER_ROLE_CODES = "user:role-codes";
    private static final String CACHE_USER_PERMS = "user:perms";
    private static final String ALL_CACHE_KEY = "all";

    private final CacheConsistencyService cacheConsistencyService;

    public SystemCacheEvictor(CacheConsistencyService cacheConsistencyService) {
        this.cacheConsistencyService = cacheConsistencyService;
    }

    public void evictSystemUsers() {
        afterCommit(() -> evict(CACHE_SYSTEM_USERS, ALL_CACHE_KEY));
    }

    public void evictSystemRoles() {
        afterCommit(() -> evict(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY));
    }

    public void evictSystemMenus() {
        afterCommit(() -> evict(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY));
    }

    public void evictUserAuth(Long userId) {
        afterCommit(() -> {
            evict(CACHE_USER_ROLE_CODES, userId);
            evict(CACHE_USER_PERMS, userId);
            evict(CACHE_USER_MENU_TREE, userId);
        });
    }

    public void evictUserAuthBatch(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            evictUserAuth(userId);
        }
    }

    private void evict(String cacheName, Object key) {
        cacheConsistencyService.deleteWithDelay(cacheName, key);
    }

    private void afterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
