package com.jianqing.framework.cache;

import java.util.Collection;

public interface CacheConsistencyService {

    void deleteWithDelay(String cacheName, Object key);

    void deleteWithDelay(String cacheName, Collection<?> keys);
}
