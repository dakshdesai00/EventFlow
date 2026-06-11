package dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateEnvironmentVariableFunctionRequest(

        @NotBlank(message = "Key cannot be blank")
        String key,

        @NotBlank(message = "Value cannot be blank")
        String value

) {
}