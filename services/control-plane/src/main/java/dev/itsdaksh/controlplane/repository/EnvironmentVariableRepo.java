package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvironmentVariableRepo extends JpaRepository<EnvironmentVariable, Long> {
    List<EnvironmentVariable> findByProjectId(Long projectId);
    List<EnvironmentVariable> findByProjectIdAndFunctionIsNull(
            Long projectId
    );

    List<EnvironmentVariable> findByFunctionId(Long functionId);
    
    Optional<EnvironmentVariable> findByProjectIdAndKeyAndFunctionIsNull(Long projectId, String key);
    Optional<EnvironmentVariable> findByFunctionIdAndKey(Long functionId, String key);
}
