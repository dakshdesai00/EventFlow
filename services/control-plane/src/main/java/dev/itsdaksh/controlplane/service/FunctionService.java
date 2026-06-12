package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.FunctionRequests.CreateFunctionRequest;
import dev.itsdaksh.controlplane.dto.FunctionRequests.FunctionResponse;
import dev.itsdaksh.controlplane.entity.Function;
import dev.itsdaksh.controlplane.repository.FunctionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final FunctionRepo functionRepo;
    private final ProjectService projectService;

    public Optional<FunctionResponse> createFunction(
            Long projectId,
            CreateFunctionRequest request
    ) {

        return projectService.getProjectById(projectId)
                .map(project -> {

                    Function function =
                            Function.builder()
                                    .project(project)
                                    .name(request.name())
                                    .description(request.description())
                                    .timeoutMs(request.timeoutMs())
                                    .memoryLimitMb(request.memoryLimitMb())
                                    .cacheEnabled(request.cacheEnabled())
                                    .cacheTtlSeconds(request.cacheTtlSeconds())
                                    .build();

                    function = functionRepo.save(function);

                    return mapToResponse(function);
                });
    }

    public Optional<List<FunctionResponse>> getProjectFunctions(
            Long projectId
    ) {

        return projectService.getProjectById(projectId)
                .map(project ->
                        functionRepo.findByProjectId(projectId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList()
                );
    }

    public Optional<FunctionResponse> getFunction(
            Long functionId
    ) {

        return functionRepo.findById(functionId)
                .map(this::mapToResponse);
    }

    public Optional<FunctionResponse> updateFunction(
            Long functionId,
            CreateFunctionRequest request
    ) {

        return functionRepo.findById(functionId)
                .map(function -> {

                    function.setName(request.name());
                    function.setDescription(request.description());
                    function.setTimeoutMs(request.timeoutMs());
                    function.setMemoryLimitMb(request.memoryLimitMb());
                    function.setCacheEnabled(request.cacheEnabled());
                    function.setCacheTtlSeconds(request.cacheTtlSeconds());

                    return mapToResponse(
                            functionRepo.save(function)
                    );
                });
    }

    public Optional<FunctionResponse> deleteFunction(
            Long functionId
    ) {

        return functionRepo.findById(functionId)
                .map(function -> {

                    functionRepo.delete(function);

                    return mapToResponse(function);
                });
    }

    public Optional<Function> getFunctionEntity(Long functionId) {
        return functionRepo.findById(functionId);
    }

    public Function saveFunctionEntity(Function function) {
        return functionRepo.save(function);
    }

    private FunctionResponse mapToResponse(
            Function function
    ) {

        return new FunctionResponse(
                function.getId(),
                function.getProject().getId(),
                function.getName(),
                function.getDescription(),
                function.getTimeoutMs(),
                function.getMemoryLimitMb(),
                function.getCacheEnabled(),
                function.getCacheTtlSeconds(),
                function.getActiveVersion() != null
                        ? function.getActiveVersion().getId()
                        : null
        );
    }
}