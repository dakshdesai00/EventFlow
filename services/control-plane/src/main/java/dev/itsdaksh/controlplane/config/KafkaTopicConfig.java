package dev.itsdaksh.controlplane.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic functionExecutionsTopic() {

        return new NewTopic(
                "function-executions",
                6,
                (short) 1
        );
    }

    @Bean
    public NewTopic retryExecutionsTopic() {

        return new NewTopic(
                "retry-executions",
                2,
                (short) 1
        );
    }

    @Bean
    public NewTopic dlqTopic() {

        return new NewTopic(
                "function-executions-dlq",
                1,
                (short) 1
        );
    }
}