package dev.itsdaksh.controlplane.dto.FunctionRequests;


public record CreateFunctionRequest(
        String name,

        String description,

        Integer timeoutMs,

        Integer memoryLimitMb,

        Boolean cacheEnabled,

        Integer cacheTtlSeconds
) {
}