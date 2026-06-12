package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.ExecutionRequests.ExecutionLogResponse;
import dev.itsdaksh.controlplane.dto.ExecutionRequests.ExecutionResponse;
import dev.itsdaksh.controlplane.entity.Execution;
import dev.itsdaksh.controlplane.entity.ExecutionLog;
import dev.itsdaksh.controlplane.entity.User;
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
    private final FunctionService functionService;
    private final EventService eventService;
    private final CurrentUserService currentUserService;
    public Optional<ExecutionResponse> getExecution(
            Long executionId
    ) {
        return getExecutionEntity(executionId)
                .map(this::mapExecution);
    }
    public List<ExecutionResponse> getFunctionExecutions(
            Long functionId
    ) {
        return functionService.getFunctionEntity(functionId)
                .map(function ->
                        executionRepo
                                .findByFunctionIdOrderByStartedAtDesc(
                                        functionId
                                )
                                .stream()
                                .map(this::mapExecution)
                                .toList()
                )
                .orElse(List.of());
    }
    public List<ExecutionResponse> getEventExecutions(
            Long eventId
    ) {
        return eventService.getEventEntity(eventId)
                .map(event ->
                        executionRepo
                                .findByEventIdOrderByStartedAtDesc(
                                        eventId
                                )
                                .stream()
                                .map(this::mapExecution)
                                .toList()
                )
                .orElse(List.of());
    }
    public List<ExecutionLogResponse> getExecutionLogs(
            Long executionId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();
        return executionLogRepo
                .findByExecutionIdAndExecutionFunctionProjectUserId(
                        executionId,
                        currentUser.getId()
                )
                .stream()
                .map(this::mapLog)
                .toList();
    }
    private Optional<Execution> getExecutionEntity(
            Long executionId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();
        return executionRepo
                .findByIdAndFunctionProjectUserId(
                        executionId,
                        currentUser.getId()
                );
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