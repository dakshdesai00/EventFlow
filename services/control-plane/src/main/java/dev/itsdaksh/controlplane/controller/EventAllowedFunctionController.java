package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests.CreateEventAllowedFunctionRequest;
import dev.itsdaksh.controlplane.service.EventAllowedFunctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EventAllowedFunctionController {

    private final EventAllowedFunctionService service;

    @PostMapping("/api/events/{eventId}/allowed-functions")
    public ResponseEntity<?> create(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventAllowedFunctionRequest request
    ) {

        return service.create(eventId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event or Function not found"));
    }

    @GetMapping("/api/events/{eventId}/allowed-functions")
    public ResponseEntity<?> getAll(
            @PathVariable Long eventId
    ) {

        return service.getAll(eventId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }

    @DeleteMapping("/api/event-allowed-functions/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ) {

        return service.delete(id)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Allowed function deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Allowed function not found"));
    }
}