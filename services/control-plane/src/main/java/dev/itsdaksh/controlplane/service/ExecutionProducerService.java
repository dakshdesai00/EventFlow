package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.Kafka.ExecutionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionProducerService {

    @Value("${queue.mode:KAFKA}")
    private String queueMode;

    private final KafkaTemplate<
            String,
            ExecutionMessage
            > kafkaTemplate;

    public void publishExecution(
            Long executionId
    ) {
        if ("POSTGRES".equalsIgnoreCase(queueMode)) {
            return;
        }

        kafkaTemplate.send(
                "function-executions",
                executionId.toString(),
                new ExecutionMessage(
                        executionId,
                        0
                )
        );
    }
}