package com.wine.to.up.demo.service.messaging.serialization;

import com.wine.to.up.demo.service.api.message.MessageSentToKafkaEventOuterClass.MessageSentToKafkaEvent;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serializer for {@link MessageSentToKafkaEvent}
 */
public class EventSerializer implements Serializer<MessageSentToKafkaEvent> {
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize(String topic, MessageSentToKafkaEvent data) {
        return data.toByteArray();
    }
}
