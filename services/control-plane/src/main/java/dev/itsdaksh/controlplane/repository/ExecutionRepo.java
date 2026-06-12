package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepo
        extends JpaRepository<Execution, Long> {

    List<Execution> findByFunctionId(Long functionId);

    List<Execution> findByEventId(Long eventId);
    List<Execution> findByFunctionIdOrderByStartedAtDesc(
            Long functionId
    );

    List<Execution> findByEventIdOrderByStartedAtDesc(
            Long eventId
    );
}