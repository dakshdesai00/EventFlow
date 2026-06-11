package dev.itsdaksh.controlplane.dto.EventRequests;

public record EventResponse(
        Long id,
        String name,
        String description,
        String webhookToken,
        Long projectId
) {
}
