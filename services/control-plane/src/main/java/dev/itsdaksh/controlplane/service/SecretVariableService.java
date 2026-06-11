package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.SecretVariableRequests.SecretVariableResponse;
import dev.itsdaksh.controlplane.entity.SecretVariable;
import dev.itsdaksh.controlplane.repository.ProjectRepo;
import dev.itsdaksh.controlplane.repository.SecretVariableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecretVariableService {

    private final SecretVariableRepo secretVariableRepo;
    private final ProjectRepo projectRepo;

    public Optional<SecretVariableResponse> saveProjectSecretVariable(
            Long projectId,
            CreateSecretVariableProjectRequest sv
    ) {

        return projectRepo.findById(projectId)
                .map(project -> {

                    SecretVariable secretVariable =
                            SecretVariable.builder()
                                    .key(sv.key())
                                    .value(sv.value())
                                    .project(project)
                                    .build();

                    secretVariable =
                            secretVariableRepo.save(secretVariable);

                    return new SecretVariableResponse(
                            secretVariable.getId(),
                            secretVariable.getKey(),
                            secretVariable.getValue(),
                            projectId,
                            null
                    );
                });
    }

    public Optional<List<SecretVariableResponse>> getProjectSecretVariable(
            Long projectId
    ) {

        return projectRepo.findById(projectId)
                .map(project ->
                        secretVariableRepo.findByProjectId(projectId)
                                .stream()
                                .map(sv -> new SecretVariableResponse(
                                        sv.getId(),
                                        sv.getKey(),
                                        sv.getValue(),
                                        projectId,
                                        null
                                ))
                                .toList()
                );
    }

    public Optional<SecretVariableResponse> updateProjectSecretVariable(
            Long projectId,
            Long svId,
            CreateSecretVariableProjectRequest sv
    ) {

        return secretVariableRepo.findById(svId)
                .filter(existingSv ->
                        existingSv.getProject().getId().equals(projectId))
                .map(existingSv -> {

                    existingSv.setKey(sv.key());
                    existingSv.setValue(sv.value());

                    SecretVariable updated =
                            secretVariableRepo.save(existingSv);

                    return new SecretVariableResponse(
                            updated.getId(),
                            updated.getKey(),
                            updated.getValue(),
                            projectId,
                            null
                    );
                });
    }

    public Optional<SecretVariableResponse> deleteProjectSecretVariable(
            Long projectId,
            Long svId
    ) {

        return secretVariableRepo.findById(svId)
                .filter(sv ->
                        sv.getProject().getId().equals(projectId))
                .map(sv -> {

                    secretVariableRepo.delete(sv);

                    return new SecretVariableResponse(
                            sv.getId(),
                            sv.getKey(),
                            sv.getValue(),
                            projectId,
                            null
                    );
                });
    }
}