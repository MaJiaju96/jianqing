package com.jianqing.integration.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LocalDynamicConfigGateway implements DynamicConfigGateway {

    private final Map<String, String> configStore = new ConcurrentHashMap<>();
    private final Map<String, List<ConfigChangeListener>> listenerStore = new ConcurrentHashMap<>();

    @Override
    public String get(String dataId, String group, String defaultValue) {
        return configStore.getOrDefault(configKey(dataId, group), defaultValue);
    }

    @Override
    public void addListener(String dataId, String group, ConfigChangeListener listener) {
        listenerStore.computeIfAbsent(configKey(dataId, group), key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void publish(String dataId, String group, String value) {
        String key = configKey(dataId, group);
        configStore.put(key, value);
        List<ConfigChangeListener> listeners = listenerStore.getOrDefault(key, List.of());
        for (ConfigChangeListener listener : listeners) {
            listener.onChange(dataId, group, value);
        }
    }

    private String configKey(String dataId, String group) {
        return dataId + "@@" + group;
    }
}
