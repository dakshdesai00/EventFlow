package dev.itsdaksh.controlplane.dto.EventAllowedDomainRequests;

public record EventAllowedDomainResponse(

        Long id,

        Long eventId,

        String domain

) {
}