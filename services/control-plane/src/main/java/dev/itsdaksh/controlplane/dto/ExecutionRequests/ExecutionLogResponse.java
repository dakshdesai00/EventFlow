package dev.itsdaksh.controlplane.dto.ExecutionRequests;

import dev.itsdaksh.controlplane.entity.LogLevel;

import java.time.Instant;

public record ExecutionLogResponse(

        Long id,

        Long executionId,

        LogLevel level,

        Instant timestamp,

        String message

) {
}