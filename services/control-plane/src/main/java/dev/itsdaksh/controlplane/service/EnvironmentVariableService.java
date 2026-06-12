package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableFunctionRequest;
import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.EnvironmentVariableResponse;
import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import dev.itsdaksh.controlplane.entity.User;
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
    private final CurrentUserService currentUserService;

    public Optional<EnvironmentVariableResponse> saveProjectEnvironmentVariable(
            Long projectId,
            CreateEnvironmentVariableProjectRequest env
    ) {

        return projectService.getProjectById(projectId)
                .filter(project ->
                        environmentVariableRepo
                                .findByProjectIdAndKeyAndFunctionIsNull(
                                        projectId,
                                        env.key()
                                )
                                .isEmpty()
                )
                .map(project -> {

                    EnvironmentVariable environmentVariable =
                            EnvironmentVariable.builder()
                                    .project(project)
                                    .key(env.key())
                                    .value(env.value())
                                    .build();

                    environmentVariable =
                            environmentVariableRepo.save(
                                    environmentVariable
                            );

                    return map(
                            environmentVariable,
                            projectId,
                            null
                    );
                });
    }

    public Optional<List<EnvironmentVariableResponse>>
    getProjectEnvironmentVariable(
            Long projectId
    ) {

        return projectService.getProjectById(projectId)
                .map(project ->
                        environmentVariableRepo
                                .findByProjectIdAndFunctionIsNull(
                                        projectId
                                )
                                .stream()
                                .map(env ->
                                        map(
                                                env,
                                                projectId,
                                                null
                                        )
                                )
                                .toList()
                );
    }

    public Optional<EnvironmentVariableResponse>
    updateProjectEnvironmentVariable(
            Long projectId,
            Long envId,
            CreateEnvironmentVariableProjectRequest env
    ) {

        return projectService.getProjectById(projectId)
                .flatMap(project ->
                        getProjectEnvironmentVariableEntity(
                                envId
                        )
                )
                .map(existingEnv -> {

                    existingEnv.setKey(
                            env.key()
                    );

                    existingEnv.setValue(
                            env.value()
                    );

                    EnvironmentVariable updated =
                            environmentVariableRepo.save(
                                    existingEnv
                            );

                    return map(
                            updated,
                            projectId,
                            null
                    );
                });
    }

    public Optional<EnvironmentVariableResponse>
    deleteProjectEnvironmentVariable(
            Long projectId,
            Long envId
    ) {

        return projectService.getProjectById(projectId)
                .flatMap(project ->
                        getProjectEnvironmentVariableEntity(
                                envId
                        )
                )
                .map(env -> {

                    environmentVariableRepo.delete(env);

                    return map(
                            env,
                            projectId,
                            null
                    );
                });
    }

    public Optional<EnvironmentVariableResponse>
    saveFunctionEnvironmentVariable(
            Long functionId,
            CreateEnvironmentVariableFunctionRequest env
    ) {

        return functionService.getFunctionEntity(functionId)
                .filter(function ->
                        environmentVariableRepo
                                .findByFunctionIdAndKey(
                                        functionId,
                                        env.key()
                                )
                                .isEmpty()
                )
                .map(function -> {

                    EnvironmentVariable environmentVariable =
                            EnvironmentVariable.builder()
                                    .project(
                                            function.getProject()
                                    )
                                    .function(function)
                                    .key(env.key())
                                    .value(env.value())
                                    .build();

                    environmentVariable =
                            environmentVariableRepo.save(
                                    environmentVariable
                            );

                    return map(
                            environmentVariable,
                            function.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<List<EnvironmentVariableResponse>>
    getFunctionEnvironmentVariables(
            Long functionId
    ) {

        return functionService.getFunctionEntity(functionId)
                .map(function ->
                        environmentVariableRepo
                                .findByFunctionId(functionId)
                                .stream()
                                .map(env ->
                                        map(
                                                env,
                                                function.getProject().getId(),
                                                functionId
                                        )
                                )
                                .toList()
                );
    }

    public Optional<EnvironmentVariableResponse>
    updateFunctionEnvironmentVariable(
            Long functionId,
            Long envId,
            CreateEnvironmentVariableFunctionRequest env
    ) {

        return functionService.getFunctionEntity(functionId)
                .flatMap(function ->
                        getFunctionEnvironmentVariableEntity(
                                envId
                        )
                )
                .map(existing -> {

                    existing.setKey(
                            env.key()
                    );

                    existing.setValue(
                            env.value()
                    );

                    EnvironmentVariable updated =
                            environmentVariableRepo.save(
                                    existing
                            );

                    return map(
                            updated,
                            updated.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<EnvironmentVariableResponse>
    deleteFunctionEnvironmentVariable(
            Long functionId,
            Long envId
    ) {

        return functionService.getFunctionEntity(functionId)
                .flatMap(function ->
                        getFunctionEnvironmentVariableEntity(
                                envId
                        )
                )
                .map(env -> {

                    environmentVariableRepo.delete(env);

                    return map(
                            env,
                            env.getProject().getId(),
                            functionId
                    );
                });
    }

    private Optional<EnvironmentVariable>
    getProjectEnvironmentVariableEntity(
            Long envId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return environmentVariableRepo
                .findByIdAndProjectUserId(
                        envId,
                        currentUser.getId()
                )
                .filter(env ->
                        env.getFunction() == null
                );
    }

    private Optional<EnvironmentVariable>
    getFunctionEnvironmentVariableEntity(
            Long envId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return environmentVariableRepo
                .findByIdAndFunctionProjectUserId(
                        envId,
                        currentUser.getId()
                )
                .filter(env ->
                        env.getFunction() != null
                );
    }

    private EnvironmentVariableResponse map(
            EnvironmentVariable env,
            Long projectId,
            Long functionId
    ) {

        return new EnvironmentVariableResponse(
                env.getId(),
                env.getKey(),
                env.getValue(),
                projectId,
                functionId
        );
    }
}