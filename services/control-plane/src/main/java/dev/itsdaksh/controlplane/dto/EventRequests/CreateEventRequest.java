package dev.itsdaksh.controlplane.dto.EventRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank
        String name,

        String description,

        Boolean exposeWebhook
) {
}
