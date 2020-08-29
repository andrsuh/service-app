package com.wine.to.up.demo.service.messaging;

import com.wine.to.up.demo.service.annotations.InjectEventLogger;
import com.wine.to.up.demo.service.api.message.KafkaServiceEventOuterClass.KafkaServiceEvent;
import com.wine.to.up.demo.service.components.AppMetrics;
import com.wine.to.up.demo.service.logging.EventLogger;
import com.wine.to.up.demo.service.logging.NotableEvents;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Service for sending messages
 */
@Slf4j
public class KafkaMessageSender<T> {
    /**
     * Producer that is configured for sending {@link KafkaServiceEvent}
     */
    private final KafkaProducer<String, T> producer;
    /**
     * Topic to send to
     */
    private final String topicName;

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

    public KafkaMessageSender(KafkaProducer<String, T> producer,
                              String topicName,
                              AppMetrics appMetrics) {
        this.appMetrics = appMetrics;
        this.producer = producer;
        this.topicName = topicName;
    }

    /**
     * Sends a single message to the topic
     */
    public void sendMessage(T message) {
        ProducerRecord<String, T> record = new ProducerRecord<>(topicName, message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                eventLogger.warn(NotableEvents.W_KAFKA_SEND_MESSAGE_FAILED, topicName);
                return;
            }
            log.debug("Message sent to Kafka topic: {}, event: {}", topicName, message);
            appMetrics.countKafkaMessageSent(topicName);
        });
    }
}
