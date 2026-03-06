package com.jianqing.framework.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jianqing.cache")
public class CacheProperties {

    private String keyPrefix;
    private long hotDataExpireSeconds;
    private long delayDoubleDeleteMillis;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getHotDataExpireSeconds() {
        return hotDataExpireSeconds;
    }

    public void setHotDataExpireSeconds(long hotDataExpireSeconds) {
        this.hotDataExpireSeconds = hotDataExpireSeconds;
    }

    public long getDelayDoubleDeleteMillis() {
        return delayDoubleDeleteMillis;
    }

    public void setDelayDoubleDeleteMillis(long delayDoubleDeleteMillis) {
        this.delayDoubleDeleteMillis = delayDoubleDeleteMillis;
    }
}
