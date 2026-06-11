package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EventSubscriptionRequests.CreateEventSubscriptionRequest;
import dev.itsdaksh.controlplane.service.EventSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EventSubscriptionController {

    private final EventSubscriptionService eventSubscriptionService;

    @PostMapping("/api/events/{eventId}/subscriptions")
    public ResponseEntity<?> createSubscription(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventSubscriptionRequest request
    ) {

        return eventSubscriptionService
                .createSubscription(eventId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Event or Function not found"));
    }

    @GetMapping("/api/events/{eventId}/subscriptions")
    public ResponseEntity<?> getEventSubscriptions(
            @PathVariable Long eventId
    ) {

        return ResponseEntity.ok(
                eventSubscriptionService
                        .getEventSubscriptions(eventId)
        );
    }

    @DeleteMapping("/api/subscriptions/{subscriptionId}")
    public ResponseEntity<?> deleteSubscription(
            @PathVariable Long subscriptionId
    ) {

        return eventSubscriptionService
                .deleteSubscription(subscriptionId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Subscription deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Subscription not found"));
    }
}