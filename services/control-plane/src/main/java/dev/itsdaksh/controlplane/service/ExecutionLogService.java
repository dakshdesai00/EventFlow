package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.entity.Execution;
import dev.itsdaksh.controlplane.entity.ExecutionLog;
import dev.itsdaksh.controlplane.entity.LogLevel;
import dev.itsdaksh.controlplane.repository.ExecutionLogRepo;
import dev.itsdaksh.controlplane.repository.ExecutionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionLogService {

    private final ExecutionRepo executionRepo;
    private final ExecutionLogRepo executionLogRepo;

    public void log(
            Long executionId,
            LogLevel level,
            String message
    ) {

        executionRepo.findById(
                        executionId
                )
                .ifPresent(execution -> {

                    ExecutionLog log =
                            ExecutionLog.builder()
                                    .execution(execution)
                                    .level(level)
                                    .message(message)
                                    .build();

                    executionLogRepo.save(log);
                });
    }
}