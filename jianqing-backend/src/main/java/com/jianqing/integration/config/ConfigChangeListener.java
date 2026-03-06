package com.jianqing.integration.config;

@FunctionalInterface
public interface ConfigChangeListener {

    void onChange(String dataId, String group, String newValue);
}
