package dev.itsdaksh.controlplane.dto.FunctionRequests;


public record FunctionResponse(

        Long id,

        Long projectId,

        String name,

        String description,

        Integer timeoutMs,

        Integer memoryLimitMb,

        Boolean cacheEnabled,

        Integer cacheTtlSeconds,

        Long activeVersionId

) {
}