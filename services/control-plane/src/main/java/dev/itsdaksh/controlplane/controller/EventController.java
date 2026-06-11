package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EventRequests.CreateEventRequest;
import dev.itsdaksh.controlplane.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/api/projects/{projectId}/events")
    public ResponseEntity<?> createEvent(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateEventRequest request
    ) {

        return eventService.saveEvent(projectId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @GetMapping("/api/projects/{projectId}/events")
    public ResponseEntity<?> getProjectEvents(
            @PathVariable Long projectId
    ) {

        return eventService.getProjectEvents(projectId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @GetMapping("/api/events/{eventId}")
    public ResponseEntity<?> getEvent(
            @PathVariable Long eventId
    ) {

        return eventService.getEvent(eventId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }

    @PutMapping("/api/events/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventRequest request
    ) {

        return eventService.updateEvent(eventId, request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }

    @DeleteMapping("/api/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long eventId
    ) {

        return eventService.deleteEvent(eventId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok("Event deleted successfully"))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event not found"));
    }
}