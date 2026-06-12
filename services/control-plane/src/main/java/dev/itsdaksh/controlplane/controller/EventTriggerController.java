package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.service.EventTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EventTriggerController {

    private final EventTriggerService eventTriggerService;

    @PostMapping("/api/events/trigger/{token}")
    public ResponseEntity<?> triggerEvent(
            @PathVariable String token,
            @RequestHeader(
                    value = "Origin",
                    required = false
            )
            String origin,
            @RequestBody Map<String, Object> payload
    ) {

        return eventTriggerService
                .triggerEvent(
                        token,
                        origin,
                        payload
                )
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(
                                HttpStatus.FORBIDDEN
                        ).body(
                                "Event not found or origin not allowed"
                        ));
    }
}