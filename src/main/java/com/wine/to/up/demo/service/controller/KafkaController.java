package com.wine.to.up.demo.service.controller;

import com.google.protobuf.ByteString;
import com.wine.to.up.commonlib.messaging.KafkaMessageSender;
import com.wine.to.up.demo.service.api.dto.DemoServiceMessage;
import com.wine.to.up.demo.service.api.message.KafkaMessageHeaderOuterClass;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * REST controller of the service
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka")
@Validated
@Slf4j
public class KafkaController {

    /**
     * Service for sending messages
     */
    private KafkaMessageSender<KafkaMessageSentEvent> kafkaSendMessageService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);


    @Autowired
    public KafkaController(KafkaMessageSender<KafkaMessageSentEvent> kafkaSendMessageService) {
        this.kafkaSendMessageService = kafkaSendMessageService;
    }

    /**
     * Sends messages into the topic "test".
     * In fact now this service listen to that topic too. That means that it causes sending and reading messages
     */
    @PostMapping(value = "/send")
    public void sendMessage(@RequestBody String message) {
        sendMessageWithHeaders(new DemoServiceMessage(Collections.emptyMap(), message));
    }

    /**
     * See {@link #sendMessage(String)}
     * Sends message with headers
     */
    @PostMapping(value = "/send/headers")
    public void sendMessageWithHeaders(@RequestBody DemoServiceMessage message) {
        AtomicInteger counter = new AtomicInteger(0);

        KafkaMessageSentEvent event = KafkaMessageSentEvent.newBuilder()
                .addAllHeaders(message.getHeaders().entrySet().stream()
                        .map(entry -> KafkaMessageHeaderOuterClass.KafkaMessageHeader.newBuilder()
                                .setKey(entry.getKey())
                                .setValue(ByteString.copyFrom(entry.getValue()))
                                .build())
                        .collect(toList()))
                .setMessage(message.getMessage())
                .build();

        int sent = Stream.iterate(1, v -> v + 1)
                .limit(3)
                .map(n -> executorService.submit(() -> {
                    int numOfMessages = 10;
                    for (int j = 0; j < numOfMessages; j++) {
                        kafkaSendMessageService.sendMessage(event);
                        counter.incrementAndGet();
                    }
                    return numOfMessages;
                }))
                .map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error while sending in Kafka ", e);
                        return 0;
                    }
                })
                .mapToInt(Integer::intValue)
                .sum();

        log.info("Sent: " + sent);
    }
}
