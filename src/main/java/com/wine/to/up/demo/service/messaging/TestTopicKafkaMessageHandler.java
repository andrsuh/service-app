package com.wine.to.up.demo.service.messaging;

import com.wine.to.up.demo.service.api.message.KafkaServiceEventOuterClass.KafkaServiceEvent;
import com.wine.to.up.demo.service.domain.entity.Message;
import com.wine.to.up.demo.service.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class TestTopicKafkaMessageHandler implements KafkaMessageHandler<KafkaServiceEvent> {
    private final MessageRepository messageRepository;

    private final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    public TestTopicKafkaMessageHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void handle(KafkaServiceEvent message) {
        counter.incrementAndGet();
        log.info("Message received from test topic: test, number of messages: {}", counter.get());
        messageRepository.save(new Message(message.getMessage()));
    }
}