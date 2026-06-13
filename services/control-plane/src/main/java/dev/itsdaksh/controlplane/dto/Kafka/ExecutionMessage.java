package dev.itsdaksh.controlplane.dto.Kafka;

public record ExecutionMessage(
        Long executionId,
        Integer attempt
) {

}