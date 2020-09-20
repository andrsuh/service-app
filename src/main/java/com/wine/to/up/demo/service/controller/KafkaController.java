package com.wine.to.up.demo.service.controller;

import com.google.protobuf.ByteString;
import com.wine.to.up.commonlib.annotations.InjectEventLogger;
import com.wine.to.up.commonlib.logging.CommonNotableEvents;
import com.wine.to.up.commonlib.logging.EventLogger;
import com.wine.to.up.commonlib.messaging.KafkaMessageSender;
import com.wine.to.up.demo.service.api.dto.DemoServiceMessage;
import com.wine.to.up.demo.service.api.message.KafkaMessageHeaderOuterClass;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import com.wine.to.up.demo.service.logging.DemoServiceNotableEvents;
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
@RequestMapping("/kafka")
@Validated
@Slf4j
public class KafkaController {

    /**
     * Service for sending messages
     */
    private final KafkaMessageSender<KafkaMessageSentEvent> kafkaSendMessageService;

    /**
     * Executor service
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @SuppressWarnings("unused")
    @InjectEventLogger
    private EventLogger eventLogger;


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
        eventLogger.info(DemoServiceNotableEvents.I_CONTROLLER_RECEIVED_MESSAGE, message);
        sendMessageWithHeaders(new DemoServiceMessage(Collections.emptyMap(), message));
    }

    /**
     * See {@link #sendMessage(String)}
     * Sends message with headers
     */
    @PostMapping(value = "/send/headers")
    public void sendMessageWithHeaders(@RequestBody DemoServiceMessage message) {
        AtomicInteger counter = new AtomicInteger(0);
        eventLogger.warn(DemoServiceNotableEvents.W_SOME_WARN_EVENT, "Demo warning message");

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
                        eventLogger.info(DemoServiceNotableEvents.I_KAFKA_SEND_MESSAGE_SUCCESS, message);
                    }
                    return numOfMessages;
                }))
                .map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error while sending in Kafka ", e);
                        eventLogger.warn(CommonNotableEvents.W_KAFKA_SEND_MESSAGE_FAILED, e);
                        return 0;
                    }
                })
                .mapToInt(Integer::intValue)
                .sum();

        log.info("Sent: " + sent);
        eventLogger.warn(DemoServiceNotableEvents.W_SOME_WARN_EVENT, "Demo warning message");
    }
}
