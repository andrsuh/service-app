package com.wine.to.up.demo.service.messaging.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wine.to.up.demo.service.api.message.MessageSentToKafkaEventOuterClass.MessageSentToKafkaEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer for {@link MessageSentToKafkaEvent}
 */
@Slf4j
public class EventDeserializer implements Deserializer<MessageSentToKafkaEvent> {
    /**
     * {@inheritDoc}
     */
    @Override
    public MessageSentToKafkaEvent deserialize(String topic, byte[] bytes) {
        try {
            return MessageSentToKafkaEvent.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to deserialize message from topic: {}. {}", topic, e);
            return null;
        }
    }
}
