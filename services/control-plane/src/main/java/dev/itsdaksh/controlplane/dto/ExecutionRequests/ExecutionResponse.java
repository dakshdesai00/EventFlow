package dev.itsdaksh.controlplane.dto.ExecutionRequests;


import dev.itsdaksh.controlplane.entity.ExecutionStatus;

import java.time.Instant;

public record ExecutionResponse(

        Long id,

        Long eventId,

        Long functionId,

        ExecutionStatus status,

        Instant startedAt,

        Instant endedAt,

        Long durationMs,

        String errorMessage

) {
}