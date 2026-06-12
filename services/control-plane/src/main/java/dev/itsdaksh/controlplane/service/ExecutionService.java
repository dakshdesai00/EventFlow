package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.ExecutionRequests.ExecutionLogResponse;
import dev.itsdaksh.controlplane.dto.ExecutionRequests.ExecutionResponse;
import dev.itsdaksh.controlplane.entity.Execution;
import dev.itsdaksh.controlplane.entity.ExecutionLog;
import dev.itsdaksh.controlplane.repository.ExecutionLogRepo;
import dev.itsdaksh.controlplane.repository.ExecutionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final ExecutionRepo executionRepo;
    private final ExecutionLogRepo executionLogRepo;

    public Optional<ExecutionResponse> getExecution(
            Long executionId
    ) {

        return executionRepo.findById(executionId)
                .map(this::mapExecution);
    }

    public List<ExecutionResponse> getFunctionExecutions(
            Long functionId
    ) {

        return executionRepo.findByFunctionId(functionId)
                .stream()
                .map(this::mapExecution)
                .toList();
    }

    public List<ExecutionResponse> getEventExecutions(
            Long eventId
    ) {

        return executionRepo.findByEventId(eventId)
                .stream()
                .map(this::mapExecution)
                .toList();
    }

    public List<ExecutionLogResponse> getExecutionLogs(
            Long executionId
    ) {

        return executionLogRepo.findByExecutionId(executionId)
                .stream()
                .map(this::mapLog)
                .toList();
    }

    private ExecutionResponse mapExecution(
            Execution execution
    ) {

        return new ExecutionResponse(
                execution.getId(),
                execution.getEvent().getId(),
                execution.getFunction().getId(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getEndedAt(),
                execution.getDurationMs(),
                execution.getErrorMessage()
        );
    }

    private ExecutionLogResponse mapLog(
            ExecutionLog log
    ) {

        return new ExecutionLogResponse(
                log.getId(),
                log.getExecution().getId(),
                log.getLevel(),
                log.getTimestamp(),
                log.getMessage()
        );
    }
}