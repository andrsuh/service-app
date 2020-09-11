package com.wine.to.up.demo.service.configuration;

import com.wine.to.up.commonlib.messaging.BaseKafkaHandler;
import com.wine.to.up.commonlib.messaging.KafkaMessageHandler;
import com.wine.to.up.commonlib.messaging.KafkaMessageSender;
import com.wine.to.up.demo.service.api.DemoServiceApiProperties;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import com.wine.to.up.demo.service.components.DemoServiceMetricsCollector;
import com.wine.to.up.demo.service.messaging.TestTopicKafkaMessageHandler;
import com.wine.to.up.demo.service.messaging.serialization.EventDeserializer;
import com.wine.to.up.demo.service.messaging.serialization.EventSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {
    /**
     * List of kafka servers
     */
    @Value("${spring.kafka.bootstrap-server}")
    private String brokers;

    /**
     * Application consumer group id
     */
    @Value("${spring.kafka.consumer.group-id}")
    private String applicationConsumerGroupId;

    /**
     * Creating general producer properties. Common for all the producers
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Properties producerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return properties;
    }

    /**
     * Creating general consumer properties. Common for all the consumers
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Properties consumerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, applicationConsumerGroupId);
        //in case of consumer crashing, new consumer will read all messages from committed offset
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }


    /**
     * Creates consumer based on general properties.
     * <p>
     * Uses custom deserializer as the messages within single topic should be the same type. And
     * the messages in different topics can have different types and require different deserializers
     * <p>
     * Binds the consumer of the topic with the object which is responsible for handling messages from
     * this topic
     * <p>
     * From now on all the messages consumed from given topic will be delegate
     * to {@link KafkaMessageHandler#handle(Object)} of the given handler
     *
     * @param consumerProperties is the general consumer properties. {@link #consumerProperties()}
     * @param handler            which is responsible for handling messages from this topic
     */
    //TODO create-service: use your DemoServiceApiProperties, rename to reflect your topic name
    @Bean
    BaseKafkaHandler<KafkaMessageSentEvent> testTopicMessagesHandler(Properties consumerProperties,
                                                                     DemoServiceApiProperties demoServiceApiProperties,
                                                                     TestTopicKafkaMessageHandler handler) {
        // set appropriate deserializer for value
        consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class.getName());

        // bind consumer with topic name and with appropriate handler
        return new BaseKafkaHandler<>(demoServiceApiProperties.getMessageSentEventsTopicName(), new KafkaConsumer<>(consumerProperties), handler);
    }

    /**
     * Creates sender based on general properties. It helps to send single message to designated topic.
     * <p>
     * Uses custom serializer as the messages within single topic should be the same type. And
     * the messages in different topics can have different types and require different serializers
     *
     * @param producerProperties       is the general producer properties. {@link #producerProperties()}
     * @param demoServiceApiProperties class containing the values of the given service's API properties (in this particular case topic name)
     * @param metricsCollector         class encapsulating the logic of the metrics collecting and publishing
     */
    //TODO create-service: rename to reflect your topic name
    @Bean
    KafkaMessageSender<KafkaMessageSentEvent> testTopicKafkaMessageSender(Properties producerProperties,
                                                                          DemoServiceApiProperties demoServiceApiProperties,
                                                                          DemoServiceMetricsCollector metricsCollector) {
        // set appropriate serializer for value
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSerializer.class.getName());

        return new KafkaMessageSender<>(new KafkaProducer<>(producerProperties), demoServiceApiProperties.getMessageSentEventsTopicName(), metricsCollector);
    }
}
