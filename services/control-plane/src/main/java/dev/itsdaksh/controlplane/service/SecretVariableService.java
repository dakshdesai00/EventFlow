package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableFunctionRequest;
import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.SecretVariableRequests.SecretVariableResponse;
import dev.itsdaksh.controlplane.entity.SecretVariable;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.SecretVariableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecretVariableService {

    private final SecretVariableRepo secretVariableRepo;
    private final ProjectService projectService;
    private final FunctionService functionService;
    private final CurrentUserService currentUserService;

    public Optional<SecretVariableResponse> saveProjectSecretVariable(
            Long projectId,
            CreateSecretVariableProjectRequest sv
    ) {

        return projectService.getProjectById(projectId)
                .filter(project ->
                        secretVariableRepo
                                .findByProjectIdAndKeyAndFunctionIsNull(
                                        projectId,
                                        sv.key()
                                )
                                .isEmpty()
                )
                .map(project -> {

                    SecretVariable secretVariable =
                            SecretVariable.builder()
                                    .project(project)
                                    .key(sv.key())
                                    .value(sv.value())
                                    .build();

                    secretVariable =
                            secretVariableRepo.save(
                                    secretVariable
                            );

                    return map(
                            secretVariable,
                            projectId,
                            null
                    );
                });
    }

    public Optional<List<SecretVariableResponse>>
    getProjectSecretVariable(
            Long projectId
    ) {

        return projectService.getProjectById(projectId)
                .map(project ->
                        secretVariableRepo
                                .findByProjectIdAndFunctionIsNull(
                                        projectId
                                )
                                .stream()
                                .map(secret ->
                                        map(
                                                secret,
                                                projectId,
                                                null
                                        )
                                )
                                .toList()
                );
    }

    public Optional<SecretVariableResponse>
    updateProjectSecretVariable(
            Long projectId,
            Long secretId,
            CreateSecretVariableProjectRequest sv
    ) {

        return projectService.getProjectById(projectId)
                .flatMap(project ->
                        getProjectSecretEntity(
                                secretId
                        )
                )
                .map(existing -> {

                    existing.setKey(
                            sv.key()
                    );

                    existing.setValue(
                            sv.value()
                    );

                    SecretVariable updated =
                            secretVariableRepo.save(
                                    existing
                            );

                    return map(
                            updated,
                            projectId,
                            null
                    );
                });
    }

    public Optional<SecretVariableResponse>
    deleteProjectSecretVariable(
            Long projectId,
            Long secretId
    ) {

        return projectService.getProjectById(projectId)
                .flatMap(project ->
                        getProjectSecretEntity(
                                secretId
                        )
                )
                .map(secret -> {

                    secretVariableRepo.delete(secret);

                    return map(
                            secret,
                            projectId,
                            null
                    );
                });
    }

    public Optional<SecretVariableResponse>
    saveFunctionSecretVariable(
            Long functionId,
            CreateSecretVariableFunctionRequest sv
    ) {

        return functionService.getFunctionEntity(functionId)
                .filter(function ->
                        secretVariableRepo
                                .findByFunctionIdAndKey(
                                        functionId,
                                        sv.key()
                                )
                                .isEmpty()
                )
                .map(function -> {

                    SecretVariable secretVariable =
                            SecretVariable.builder()
                                    .project(
                                            function.getProject()
                                    )
                                    .function(function)
                                    .key(sv.key())
                                    .value(sv.value())
                                    .build();

                    secretVariable =
                            secretVariableRepo.save(
                                    secretVariable
                            );

                    return map(
                            secretVariable,
                            function.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<List<SecretVariableResponse>>
    getFunctionSecretVariable(
            Long functionId
    ) {

        return functionService.getFunctionEntity(functionId)
                .map(function ->
                        secretVariableRepo
                                .findByFunctionId(functionId)
                                .stream()
                                .map(secret ->
                                        map(
                                                secret,
                                                function.getProject().getId(),
                                                functionId
                                        )
                                )
                                .toList()
                );
    }

    public Optional<SecretVariableResponse>
    updateFunctionSecretVariable(
            Long functionId,
            Long secretId,
            CreateSecretVariableFunctionRequest sv
    ) {

        return functionService.getFunctionEntity(functionId)
                .flatMap(function ->
                        getFunctionSecretEntity(
                                secretId
                        )
                )
                .map(existing -> {

                    existing.setKey(
                            sv.key()
                    );

                    existing.setValue(
                            sv.value()
                    );

                    SecretVariable updated =
                            secretVariableRepo.save(
                                    existing
                            );

                    return map(
                            updated,
                            updated.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<SecretVariableResponse>
    deleteFunctionSecretVariable(
            Long functionId,
            Long secretId
    ) {

        return functionService.getFunctionEntity(functionId)
                .flatMap(function ->
                        getFunctionSecretEntity(
                                secretId
                        )
                )
                .map(secret -> {

                    secretVariableRepo.delete(secret);

                    return map(
                            secret,
                            secret.getProject().getId(),
                            functionId
                    );
                });
    }

    private Optional<SecretVariable>
    getProjectSecretEntity(
            Long secretId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return secretVariableRepo
                .findByIdAndProjectUserId(
                        secretId,
                        currentUser.getId()
                )
                .filter(secret ->
                        secret.getFunction() == null
                );
    }

    private Optional<SecretVariable>
    getFunctionSecretEntity(
            Long secretId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return secretVariableRepo
                .findByIdAndFunctionProjectUserId(
                        secretId,
                        currentUser.getId()
                )
                .filter(secret ->
                        secret.getFunction() != null
                );
    }

    private SecretVariableResponse map(
            SecretVariable secret,
            Long projectId,
            Long functionId
    ) {

        return new SecretVariableResponse(
                secret.getId(),
                secret.getKey(),
                secret.getValue(),
                projectId,
                functionId
        );
    }
}