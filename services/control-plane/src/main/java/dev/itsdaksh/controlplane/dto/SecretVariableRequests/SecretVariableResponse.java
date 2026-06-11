package dev.itsdaksh.controlplane.dto.SecretVariableRequests;

public record SecretVariableResponse(
        Long id,
        String key,
        String value,
        Long project_id,
        Long function_id
) {
}
