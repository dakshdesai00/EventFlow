package dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests;

import jakarta.validation.constraints.NotNull;

public record CreateEventAllowedFunctionRequest(

        @NotNull
        Long functionId

) {
}