package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Function;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FunctionRepo extends JpaRepository<Function, Long> {

    List<Function> findByProjectId(
            Long projectId
    );

    Optional<Function> findByIdAndProjectUserId(
            Long functionId,
            Long userId
    );
}