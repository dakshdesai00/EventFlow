package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.FunctionVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FunctionVersionRepo
        extends JpaRepository<FunctionVersion, Long> {

    List<FunctionVersion> findByFunctionId(Long functionId);

    Optional<FunctionVersion> findTopByFunctionIdOrderByVersionNumberDesc(
            Long functionId
    );
}