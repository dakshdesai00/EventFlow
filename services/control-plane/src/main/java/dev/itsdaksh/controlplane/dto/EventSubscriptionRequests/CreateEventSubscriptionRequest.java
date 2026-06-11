package dev.itsdaksh.controlplane.dto.EventSubscriptionRequests;

import jakarta.validation.constraints.NotNull;

public record CreateEventSubscriptionRequest(



        @NotNull
        Long functionId

) {
}