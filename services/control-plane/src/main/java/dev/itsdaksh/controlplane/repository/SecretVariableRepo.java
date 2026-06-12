package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.SecretVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecretVariableRepo
        extends JpaRepository<SecretVariable, Long> {

    List<SecretVariable> findByProjectId(
            Long projectId
    );

    List<SecretVariable> findByProjectIdAndFunctionIsNull(
            Long projectId
    );

    List<SecretVariable> findByFunctionId(
            Long functionId
    );

    Optional<SecretVariable>
    findByProjectIdAndKeyAndFunctionIsNull(
            Long projectId,
            String key
    );

    Optional<SecretVariable>
    findByFunctionIdAndKey(
            Long functionId,
            String key
    );

    Optional<SecretVariable>
    findByIdAndProjectUserId(
            Long secretId,
            Long userId
    );

    Optional<SecretVariable>
    findByIdAndFunctionProjectUserId(
            Long secretId,
            Long userId
    );
}