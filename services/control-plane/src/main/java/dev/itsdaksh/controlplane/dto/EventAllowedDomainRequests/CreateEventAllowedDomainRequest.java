package dev.itsdaksh.controlplane.dto.EventAllowedDomainRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateEventAllowedDomainRequest(

        @NotBlank
        String domain

) {
}