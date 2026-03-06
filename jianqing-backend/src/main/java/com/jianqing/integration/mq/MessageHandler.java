package com.jianqing.integration.mq;

@FunctionalInterface
public interface MessageHandler {

    void onMessage(String topic, String tag, Object payload);
}
