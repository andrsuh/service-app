package com.wine.to.up.demo.service.service;

import com.wine.to.up.demo.service.annotations.InjectEventLogger;
import com.wine.to.up.demo.service.api.ServiceApiProperties;
import com.wine.to.up.demo.service.api.message.KafkaServiceEventOuterClass.KafkaServiceEvent;
import com.wine.to.up.demo.service.components.AppMetrics;
import com.wine.to.up.demo.service.logging.EventLogger;
import com.wine.to.up.demo.service.logging.NotableEvents;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages
 */
@Service
@Slf4j
public class KafkaSendMessageService {
    /**
     * Producer that is configured for sending {@link KafkaServiceEvent}
     */
    private final KafkaProducer<String, KafkaServiceEvent> producer;
    /**
     * Properties from api. Defines topic
     */
    private final ServiceApiProperties apiProperties;

    /**
     * Important metrics. Integrated with Micrometer
     */
    private final AppMetrics appMetrics;

    /**
     * Logger for "noticeable" (important) events
     */
    @InjectEventLogger
    @SuppressWarnings("unused")
    private EventLogger eventLogger;

    @Autowired
    public KafkaSendMessageService(KafkaProducer<String, KafkaServiceEvent> producer,
                                   AppMetrics appMetrics,
                                   ServiceApiProperties apiProperties) {
        this.appMetrics = appMetrics;
        this.producer = producer;
        this.apiProperties = apiProperties;
    }

    /**
     * Sends a single message to the topic
     */
    public void sendMessage(KafkaServiceEvent event) {
        String topicName = apiProperties.getTopicName();
        ProducerRecord<String, KafkaServiceEvent> record = new ProducerRecord<>(topicName, event);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                eventLogger.warn(NotableEvents.W_KAFKA_SEND_MESSAGE_FAILED, topicName);
                return;
            }
            log.debug("Message sent to Kafka topic: {}, event: {}", topicName, event);
            appMetrics.countKafkaMessageSent(topicName);
        });
    }
}
