package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.EnvironmentVariableResponse;
import dev.itsdaksh.controlplane.entity.EnvironmentVariable;
import dev.itsdaksh.controlplane.repository.EnvironmentVariableRepo;
import dev.itsdaksh.controlplane.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnvironmentVariableService {

    private final EnvironmentVariableRepo environmentVariableRepo;
    private final ProjectRepo projectRepo;

    public Optional<EnvironmentVariableResponse> saveProjectEnvironmentVariable(
            Long projectId,
            CreateEnvironmentVariableProjectRequest env
    ) {

        return projectRepo.findById(projectId)
                .map(project -> {

                    EnvironmentVariable environmentVariable =
                            EnvironmentVariable.builder()
                                    .key(env.key())
                                    .value(env.value())
                                    .project(project)
                                    .build();

                    environmentVariable =
                            environmentVariableRepo.save(environmentVariable);

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

        return projectRepo.findById(projectId)
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
}