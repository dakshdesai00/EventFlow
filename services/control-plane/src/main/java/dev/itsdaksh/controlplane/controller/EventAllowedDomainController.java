package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EventAllowedDomainRequests.CreateEventAllowedDomainRequest;
import dev.itsdaksh.controlplane.service.EventAllowedDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EventAllowedDomainController {

    private final EventAllowedDomainService service;

    @PostMapping("/api/events/{eventId}/domains")
    public ResponseEntity<?> create(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventAllowedDomainRequest request
    ) {

        return service.create(eventId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }

    @GetMapping("/api/events/{eventId}/domains")
    public ResponseEntity<?> getAll(
            @PathVariable Long eventId
    ) {

        return service.getAll(eventId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }

    @DeleteMapping("/api/event-domains/{domainId}")
    public ResponseEntity<?> delete(
            @PathVariable Long domainId
    ) {

        return service.delete(domainId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok("Domain deleted successfully"))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Domain not found"));
    }
}