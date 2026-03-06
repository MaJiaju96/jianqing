package com.jianqing.integration.config;

public interface DynamicConfigGateway {

    String get(String dataId, String group, String defaultValue);

    void addListener(String dataId, String group, ConfigChangeListener listener);

    void publish(String dataId, String group, String value);
}
