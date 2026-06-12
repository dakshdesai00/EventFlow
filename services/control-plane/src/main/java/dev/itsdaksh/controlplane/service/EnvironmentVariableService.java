package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableFunctionRequest;
import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.EnvironmentVariableResponse;
import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import dev.itsdaksh.controlplane.repository.EnvironmentVariableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnvironmentVariableService {

    private final EnvironmentVariableRepo environmentVariableRepo;
    private final ProjectService projectService;
    private final FunctionService functionService;

    public Optional<EnvironmentVariableResponse> saveProjectEnvironmentVariable(
            Long projectId,
            CreateEnvironmentVariableProjectRequest env
    ) {

        return projectService.getProjectById(projectId)
                .filter(project -> !environmentVariableRepo.findByProjectIdAndKeyAndFunctionIsNull(projectId, env.key()).isPresent())
                .map(project -> {

                    EnvironmentVariable environmentVariable =
                            EnvironmentVariable.builder()
                                    .key(env.key())
                                    .value(env.value())
                                    .project(project)
                                    .build();

                    environmentVariable = environmentVariableRepo.save(environmentVariable);

                    return new EnvironmentVariableResponse(
                            environmentVariable.getId(),
                            environmentVariable.getKey(),
                            environmentVariable.getValue(),
                            projectId,
                            null
                    );
                });
    }

    public Optional<List<EnvironmentVariableResponse>> getProjectEnvironmentVariable(
            Long projectId
    ) {

        return projectService.getProjectById(projectId)
                .map(project ->
                        environmentVariableRepo.findByProjectId(projectId)
                                .stream()
                                .map(env -> new EnvironmentVariableResponse(
                                        env.getId(),
                                        env.getKey(),
                                        env.getValue(),
                                        projectId,
                                        null
                                ))
                                .toList()
                );
    }

    public Optional<EnvironmentVariableResponse> updateProjectEnvironmentVariable(
            Long projectId,
            Long envId,
            CreateEnvironmentVariableProjectRequest env
    ) {

        return environmentVariableRepo.findById(envId)
                .filter(existingEnv ->
                        existingEnv.getProject().getId().equals(projectId))
                .map(existingEnv -> {

                    existingEnv.setKey(env.key());
                    existingEnv.setValue(env.value());

                    EnvironmentVariable updatedEnv =
                            environmentVariableRepo.save(existingEnv);

                    return new EnvironmentVariableResponse(
                            updatedEnv.getId(),
                            updatedEnv.getKey(),
                            updatedEnv.getValue(),
                            projectId,
                            null
                    );
                });
    }

    public Optional<EnvironmentVariableResponse> deleteProjectEnvironmentVariable(
            Long projectId,
            Long envId
    ) {

        return environmentVariableRepo.findById(envId)
                .filter(env ->
                        env.getProject().getId().equals(projectId))
                .map(env -> {

                    environmentVariableRepo.delete(env);

                    return new EnvironmentVariableResponse(
                            env.getId(),
                            env.getKey(),
                            env.getValue(),
                            projectId,
                            null
                    );
                });
    }
    public Optional<EnvironmentVariableResponse> saveFunctionEnvironmentVariable(
            Long functionId,
            CreateEnvironmentVariableFunctionRequest env
    ) {

        return functionService.getFunctionEntity(functionId)
                .filter(function -> !environmentVariableRepo.findByFunctionIdAndKey(functionId, env.key()).isPresent())
                .map(function -> {

                    EnvironmentVariable environmentVariable =
                            EnvironmentVariable.builder()
                                    .project(function.getProject())
                                    .function(function)
                                    .key(env.key())
                                    .value(env.value())
                                    .build();

                    environmentVariable =
                            environmentVariableRepo.save(environmentVariable);

                    return new EnvironmentVariableResponse(
                            environmentVariable.getId(),
                            environmentVariable.getKey(),
                            environmentVariable.getValue(),
                            function.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<List<EnvironmentVariableResponse>> getFunctionEnvironmentVariables(
            Long functionId
    ) {

        return functionService.getFunctionEntity(functionId)
                .map(function ->
                        environmentVariableRepo.findByFunctionId(functionId)
                                .stream()
                                .map(env ->
                                        new EnvironmentVariableResponse(
                                                env.getId(),
                                                env.getKey(),
                                                env.getValue(),
                                                function.getProject().getId(),
                                                functionId
                                        )
                                )
                                .toList()
                );
    }

    public Optional<EnvironmentVariableResponse> updateFunctionEnvironmentVariable(
            Long functionId,
            Long envId,
            CreateEnvironmentVariableFunctionRequest env
    ) {

        return environmentVariableRepo.findById(envId)
                .filter(existing ->
                        existing.getFunction() != null
                                && existing.getFunction().getId().equals(functionId)
                )
                .map(existing -> {

                    existing.setKey(env.key());
                    existing.setValue(env.value());

                    EnvironmentVariable updated =
                            environmentVariableRepo.save(existing);

                    return new EnvironmentVariableResponse(
                            updated.getId(),
                            updated.getKey(),
                            updated.getValue(),
                            updated.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<EnvironmentVariableResponse> deleteFunctionEnvironmentVariable(
            Long functionId,
            Long envId
    ) {

        return environmentVariableRepo.findById(envId)
                .filter(env ->
                        env.getFunction() != null
                                && env.getFunction().getId().equals(functionId)
                )
                .map(env -> {

                    environmentVariableRepo.delete(env);

                    return new EnvironmentVariableResponse(
                            env.getId(),
                            env.getKey(),
                            env.getValue(),
                            env.getProject().getId(),
                            functionId
                    );
                });
    }

}