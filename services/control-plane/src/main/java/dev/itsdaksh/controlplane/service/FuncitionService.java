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
public class FuncitionService {
    private final FunctionRepo functionRepo;
    private final ProjectService projectService;
    public Optional<FunctionResponse> saveFunction(Long projectId, CreateFunctionRequest createFunctionRequest) {
        return projectService.getProjectById(projectId)
                .map(project -> {
                    Function function = Function.builder()
                            .name(createFunctionRequest.name())
                            .description(createFunctionRequest.description())
                            .timeoutMs(createFunctionRequest.timeoutMs())
                            .memoryLimitMb(createFunctionRequest.memoryLimitMb())
                            .cacheEnabled(createFunctionRequest.cacheEnabled())
                            .cacheTtlSeconds(createFunctionRequest.cacheTtlSeconds())
                            .project(project)
                            .build();
                    function = functionRepo.save(function);
                    return new FunctionResponse(
                            function.getId(),
                            projectId,
                            function.getName(),
                            function.getDescription(),
                            function.getTimeoutMs(),
                            function.getMemoryLimitMb(),
                            function.getCacheEnabled(),
                            function.getCacheTtlSeconds()
                    );
                });
    }

    public Optional<FunctionResponse> deleteFunction(Long id) {
        return functionRepo.findById(id)
                .map(function -> {
                    functionRepo.delete(function);
                    return new FunctionResponse(
                            function.getId(),
                            function.getProject().getId(),
                            function.getName(),
                            function.getDescription(),
                            function.getTimeoutMs(),
                            function.getMemoryLimitMb(),
                            function.getCacheEnabled(),
                            function.getCacheTtlSeconds()
                    );
                });
    }

    public Optional<FunctionResponse> updateFunction(Long id, CreateFunctionRequest createFunctionRequest) {
        return functionRepo.findById(id)
                .map(function -> {
                    function.setName(createFunctionRequest.name());
                    function.setDescription(createFunctionRequest.description());
                    function.setTimeoutMs(createFunctionRequest.timeoutMs());
                    function.setMemoryLimitMb(createFunctionRequest.memoryLimitMb());
                    function.setCacheEnabled(createFunctionRequest.cacheEnabled());
                    function.setCacheTtlSeconds(createFunctionRequest.cacheTtlSeconds());
                    Function updatedFunction = functionRepo.save(function);
                    return new FunctionResponse(
                            updatedFunction.getId(),
                            updatedFunction.getProject().getId(),
                            updatedFunction.getName(),
                            updatedFunction.getDescription(),
                            updatedFunction.getTimeoutMs(),
                            updatedFunction.getMemoryLimitMb(),
                            updatedFunction.getCacheEnabled(),
                            updatedFunction.getCacheTtlSeconds()
                    );
                })
                .or(Optional::empty);
    }

    public Optional<FunctionResponse> getFunction(Long id) {
        return functionRepo.findById(id)
                .map(function -> new FunctionResponse(
                        function.getId(),
                        function.getProject().getId(),
                        function.getName(),
                        function.getDescription(),
                        function.getTimeoutMs(),
                        function.getMemoryLimitMb(),
                        function.getCacheEnabled(),
                        function.getCacheTtlSeconds()
                ));
    }

    public Optional<List<FunctionResponse>> getProjectFunction(Long projectId) {
        return Optional.of(functionRepo.findByProjectId(projectId)
                .stream()
                .map(function -> new FunctionResponse(
                        function.getId(),
                        projectId,
                        function.getName(),
                        function.getDescription(),
                        function.getTimeoutMs(),
                        function.getMemoryLimitMb(),
                        function.getCacheEnabled(),
                        function.getCacheTtlSeconds()
                ))
                .toList());
    }
}
