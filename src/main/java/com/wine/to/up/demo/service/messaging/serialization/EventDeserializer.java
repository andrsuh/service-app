package com.wine.to.up.demo.service.messaging.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer for {@link KafkaMessageSentEvent}
 */
@Slf4j
public class EventDeserializer implements Deserializer<KafkaMessageSentEvent> {
    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaMessageSentEvent deserialize(String topic, byte[] bytes) {
        try {
            return KafkaMessageSentEvent.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to deserialize message from topic: {}. {}", topic, e);
            return null;
        }
    }
}
