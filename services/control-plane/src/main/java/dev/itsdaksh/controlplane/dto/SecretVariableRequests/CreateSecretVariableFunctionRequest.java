package dev.itsdaksh.controlplane.dto.SecretVariableRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateSecretVariableFunctionRequest(
        @NotBlank
        String key,
        @NotBlank
        String value
) {
}
