package com.wine.to.up.demo.service.messaging;

public interface KafkaMessageHandler<Message> {

    void handle(Message message);
}
