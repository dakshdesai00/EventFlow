package dev.itsdaksh.controlplane.dto.SecretVariableRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateSecretVariableProjectRequest(
        @NotBlank
        String key,
        @NotBlank
        String value
) {
}
