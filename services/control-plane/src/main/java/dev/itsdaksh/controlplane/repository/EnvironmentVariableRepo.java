package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvironmentVariableRepo extends JpaRepository<EnvironmentVariable, Long> {
    List<EnvironmentVariable> findByProjectId(Long projectId);

    Optional<EnvironmentVariable> findByProjectIdAndKey(Long projectId, @NotBlank String key);
}
