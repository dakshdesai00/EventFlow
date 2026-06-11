package dev.itsdaksh.controlplane.dto.FunctionRequests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFunctionRequest(

        @NotBlank
        String name,

        String description,

        @NotNull
        Integer timeoutMs,

        @NotNull
        Integer memoryLimitMb,

        @NotNull
        Boolean cacheEnabled,

        Integer cacheTtlSeconds

) {
}