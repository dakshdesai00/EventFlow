package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionLogRepo
        extends JpaRepository<ExecutionLog, Long> {

    List<ExecutionLog> findByExecutionId(
            Long executionId
    );
    List<ExecutionLog>
    findByExecutionIdAndExecutionFunctionProjectUserId(
            Long executionId,
            Long userId
    );

}