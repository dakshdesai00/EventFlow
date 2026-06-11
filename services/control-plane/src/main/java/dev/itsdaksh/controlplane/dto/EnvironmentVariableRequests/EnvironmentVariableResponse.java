package dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests;

public record EnvironmentVariableResponse(
        Long id,
        String key,
        String value,
        Long project_id,
        Long function_id
) {
}
