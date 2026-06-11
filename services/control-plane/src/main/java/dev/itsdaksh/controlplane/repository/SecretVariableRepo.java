package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.SecretVariable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecretVariableRepo extends JpaRepository<SecretVariable, Long> {
    List<SecretVariable> findByProjectId(Long projectId);
    Optional<SecretVariable> findByProjectIdAndKey(Long projectId, @NotBlank String key);
}
