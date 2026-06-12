package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import dev.itsdaksh.controlplane.entity.Function;
import dev.itsdaksh.controlplane.entity.SecretVariable;
import dev.itsdaksh.controlplane.repository.EnvironmentVariableRepo;
import dev.itsdaksh.controlplane.repository.FunctionRepo;
import dev.itsdaksh.controlplane.repository.SecretVariableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VariableResolutionService {

    private final FunctionRepo functionRepo;

    private final EnvironmentVariableRepo environmentVariableRepo;

    private final SecretVariableRepo secretVariableRepo;

    public Optional<Map<String, String>> resolveEnvironment(
            Long functionId
    ) {

        return functionRepo.findById(functionId)
                .map(this::buildEnvironmentMap);
    }

    public Optional<Map<String, String>> resolveSecrets(
            Long functionId
    ) {

        return functionRepo.findById(functionId)
                .map(this::buildSecretMap);
    }

    private Map<String, String> buildEnvironmentMap(
            Function function
    ) {

        Map<String, String> result =
                new HashMap<>();

        environmentVariableRepo
                .findByProjectIdAndFunctionIsNull(
                        function.getProject().getId()
                )
                .forEach(env ->
                        result.put(
                                env.getKey(),
                                env.getValue()
                        )
                );

        environmentVariableRepo
                .findByFunctionId(
                        function.getId()
                )
                .forEach(env ->
                        result.put(
                                env.getKey(),
                                env.getValue()
                        )
                );

        return result;
    }

    private Map<String, String> buildSecretMap(
            Function function
    ) {

        Map<String, String> result =
                new HashMap<>();

        secretVariableRepo
                .findByProjectIdAndFunctionIsNull(
                        function.getProject().getId()
                )
                .forEach(secret ->
                        result.put(
                                secret.getKey(),
                                secret.getValue()
                        )
                );

        secretVariableRepo
                .findByFunctionId(
                        function.getId()
                )
                .forEach(secret ->
                        result.put(
                                secret.getKey(),
                                secret.getValue()
                        )
                );

        return result;
    }
}