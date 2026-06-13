package dev.itsdaksh.controlplane.config;

import dev.itsdaksh.controlplane.dto.Kafka.ExecutionMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, ExecutionMessage> producerFactory() {

        return new DefaultKafkaProducerFactory<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        "localhost:9092"
                ),
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, ExecutionMessage> kafkaTemplate() {

        return new KafkaTemplate<>(
                producerFactory()
        );
    }
}