package com.jianqing.integration.mq;

public interface MessageBusGateway {

    void send(String topic, String tag, Object payload);

    void subscribe(String topic, String group, MessageHandler handler);
}
