package com.jianqing.integration.mq;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryMessageBusGateway implements MessageBusGateway {

    private final Map<String, List<MessageHandler>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void send(String topic, String tag, Object payload) {
        List<MessageHandler> handlers = subscribers.getOrDefault(topic, List.of());
        for (MessageHandler handler : handlers) {
            handler.onMessage(topic, tag, payload);
        }
    }

    @Override
    public void subscribe(String topic, String group, MessageHandler handler) {
        subscribers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(handler);
    }
}
