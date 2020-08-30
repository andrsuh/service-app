package com.wine.to.up.demo.service.messaging;

import com.wine.to.up.commonlib.messaging.KafkaMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class SmopikTopicKafkaMessageHandler implements KafkaMessageHandler<String> {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void handle(String message) {
        counter.incrementAndGet();
        log.info("MMMMMMMMMmmmmmmmmessage received from test topic: test, number of messages: {}", counter.get());
    }
}
