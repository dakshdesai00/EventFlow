package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableFunctionRequest;
import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableProjectRequest;
import dev.itsdaksh.controlplane.dto.SecretVariableRequests.SecretVariableResponse;
import dev.itsdaksh.controlplane.entity.SecretVariable;

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


    public Optional<SecretVariableResponse> saveProjectSecretVariable(
            Long projectId,
            CreateSecretVariableProjectRequest sv
    ) {

        return projectService.getProjectById(projectId)
                .filter(project -> !secretVariableRepo.findByProjectIdAndKeyAndFunctionIsNull(projectId, sv.key()).isPresent())
                .map(project -> {

                    SecretVariable secretVariable =
                            SecretVariable.builder()
                                    .key(sv.key())
                                    .value(sv.value())
                                    .project(project)
                                    .build();

                    secretVariable = secretVariableRepo.save(secretVariable);

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

        return projectService.getProjectById(projectId)
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
    public Optional<SecretVariableResponse> saveFunctionSecretVariable(
            Long functionId,
            CreateSecretVariableFunctionRequest sv
    ) {

        return functionService.getFunctionEntity(functionId)
                .filter(function -> !secretVariableRepo.findByFunctionIdAndKey(functionId, sv.key()).isPresent())
                .map(function -> {

                    SecretVariable secretVariable =
                            SecretVariable.builder()
                                    .project(function.getProject())
                                    .function(function)
                                    .key(sv.key())
                                    .value(sv.value())
                                    .build();

                    secretVariable =
                            secretVariableRepo.save(secretVariable);

                    return new SecretVariableResponse(
                            secretVariable.getId(),
                            secretVariable.getKey(),
                            secretVariable.getValue(),
                            function.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<List<SecretVariableResponse>> getFunctionSecretVariable(
            Long functionId
    ) {

        return functionService.getFunctionEntity(functionId)
                .map(function ->
                        secretVariableRepo.findByFunctionId(functionId)
                                .stream()
                                .map(sv ->
                                        new SecretVariableResponse(
                                                sv.getId(),
                                                sv.getKey(),
                                                sv.getValue(),
                                                function.getProject().getId(),
                                                functionId
                                        )
                                )
                                .toList()
                );
    }

    public Optional<SecretVariableResponse> updateFunctionSecretVariable(
            Long functionId,
            Long svId,
            CreateSecretVariableFunctionRequest sv
    ) {

        return secretVariableRepo.findById(svId)
                .filter(existing ->
                        existing.getFunction() != null
                                && existing.getFunction().getId().equals(functionId)
                )
                .map(existing -> {

                    existing.setKey(sv.key());
                    existing.setValue(sv.value());

                    SecretVariable updated =
                            secretVariableRepo.save(existing);

                    return new SecretVariableResponse(
                            updated.getId(),
                            updated.getKey(),
                            updated.getValue(),
                            updated.getProject().getId(),
                            functionId
                    );
                });
    }

    public Optional<SecretVariableResponse> deleteFunctionSecretVariable(
            Long functionId,
            Long svId
    ) {

        return secretVariableRepo.findById(svId)
                .filter(sv ->
                        sv.getFunction() != null
                                && sv.getFunction().getId().equals(functionId)
                )
                .map(sv -> {

                    secretVariableRepo.delete(sv);

                    return new SecretVariableResponse(
                            sv.getId(),
                            sv.getKey(),
                            sv.getValue(),
                            sv.getProject().getId(),
                            functionId
                    );
                });
    }
}