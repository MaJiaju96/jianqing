package com.jianqing.module.system.service.impl;

import com.jianqing.framework.cache.CacheConsistencyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemCacheEvictorTest {

    @Mock
    private CacheConsistencyService cacheConsistencyService;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void shouldEvictImmediatelyWhenNoTransactionActive() {
        SystemCacheEvictor evictor = new SystemCacheEvictor(cacheConsistencyService);

        evictor.evictSystemUsers();

        verify(cacheConsistencyService).deleteWithDelay("system:users", "all");
    }

    @Test
    void shouldDelayUserAuthEvictionUntilAfterCommit() {
        SystemCacheEvictor evictor = new SystemCacheEvictor(cacheConsistencyService);
        beginTransactionSynchronization();

        evictor.evictUserAuth(9L);

        verify(cacheConsistencyService, never()).deleteWithDelay("user:role-codes", 9L);
        verify(cacheConsistencyService, never()).deleteWithDelay("user:perms", 9L);
        verify(cacheConsistencyService, never()).deleteWithDelay("user:menu-tree", 9L);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(cacheConsistencyService).deleteWithDelay("user:role-codes", 9L);
        verify(cacheConsistencyService).deleteWithDelay("user:perms", 9L);
        verify(cacheConsistencyService).deleteWithDelay("user:menu-tree", 9L);
    }

    @Test
    void shouldEvictDictDataAndOptionsTogether() {
        SystemCacheEvictor evictor = new SystemCacheEvictor(cacheConsistencyService);

        evictor.evictSystemDictData("sys_common_status");

        verify(cacheConsistencyService).deleteWithDelay("system:dict-data", "sys_common_status");
        verify(cacheConsistencyService).deleteWithDelay("system:dict-options", "sys_common_status");
    }

    @Test
    void shouldEvictConfigListCache() {
        SystemCacheEvictor evictor = new SystemCacheEvictor(cacheConsistencyService);

        evictor.evictSystemConfigs();

        verify(cacheConsistencyService).deleteWithDelay("system:configs", "all");
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }
}
