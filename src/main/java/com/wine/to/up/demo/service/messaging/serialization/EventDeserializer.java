package com.wine.to.up.demo.service.messaging.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wine.to.up.demo.service.api.message.KafkaServiceEventOuterClass.KafkaServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer for {@link KafkaServiceEvent}
 */
@Slf4j
public class EventDeserializer implements Deserializer<KafkaServiceEvent> {
    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaServiceEvent deserialize(String topic, byte[] bytes) {
        try {
            return KafkaServiceEvent.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to deserialize message from topic: {}. {}", topic, e);
            return null;
        }
    }
}
