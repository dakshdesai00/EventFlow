package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @GetMapping("/api/executions/{executionId}")
    public ResponseEntity<?> getExecution(
            @PathVariable Long executionId
    ) {

        return executionService.getExecution(executionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/api/functions/{functionId}/executions")
    public ResponseEntity<?> getFunctionExecutions(
            @PathVariable Long functionId
    ) {

        return ResponseEntity.ok(
                executionService.getFunctionExecutions(
                        functionId
                )
        );
    }

    @GetMapping("/api/events/{eventId}/executions")
    public ResponseEntity<?> getEventExecutions(
            @PathVariable Long eventId
    ) {

        return ResponseEntity.ok(
                executionService.getEventExecutions(
                        eventId
                )
        );
    }

    @GetMapping("/api/executions/{executionId}/logs")
    public ResponseEntity<?> getExecutionLogs(
            @PathVariable Long executionId
    ) {

        return ResponseEntity.ok(
                executionService.getExecutionLogs(
                        executionId
                )
        );
    }
}