package dev.itsdaksh.controlplane.dto.EventSubscriptionRequests;

public record EventSubscriptionResponse(

        Long id,
        Long eventId,
        Long functionId

) {
}