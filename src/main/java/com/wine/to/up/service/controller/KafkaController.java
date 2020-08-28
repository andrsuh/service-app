package com.wine.to.up.service.controller;

import com.google.protobuf.ByteString;
import com.wine.to.up.api.dto.ServiceMessage;
import com.wine.to.up.api.message.KafkaMessageHeaderOuterClass.KafkaMessageHeader;
import com.wine.to.up.service.service.KafkaSendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.wine.to.up.api.message.KafkaServiceEventOuterClass.KafkaServiceEvent;
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
    private KafkaSendMessageService kafkaSendMessageService;

    @Autowired
    public KafkaController(KafkaSendMessageService kafkaSendMessageService) {
        this.kafkaSendMessageService = kafkaSendMessageService;
    }

    /**
     * Sends messages into the topic "test".
     * In fact now this service listen to that topic too. That means that it causes sending and reading messages
     */
    @PostMapping(value = "/send")
    public void sendMessage(@RequestBody String message) {
        sendMessageWithHeaders(new ServiceMessage(Collections.emptyMap(), message));
    }

    /**
     * See {@link #sendMessage(String)}
     * Sends message with headers
     */
    @PostMapping(value = "/send/headers")
    public void sendMessageWithHeaders(@RequestBody ServiceMessage message) {
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Headers headers = new RecordHeaders();
        message.getHeaders().forEach(headers::add);
        KafkaServiceEvent event = KafkaServiceEvent.newBuilder()
                .addAllHeaders(message.getHeaders().entrySet().stream()
                        .map(entry -> KafkaMessageHeader.newBuilder()
                                .setKey(entry.getKey())
                                .setValue(ByteString.copyFrom(entry.getValue()))
                                .build())
                        .collect(toList()))
                .setMessage(message.getMessage())
                .build();

        int sent = Stream.iterate(1, v -> v + 1)
                .limit(10)
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
