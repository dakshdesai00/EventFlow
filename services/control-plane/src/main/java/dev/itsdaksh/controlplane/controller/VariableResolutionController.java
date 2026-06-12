package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.service.VariableResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/functions")
@RequiredArgsConstructor
public class VariableResolutionController {

    private final VariableResolutionService variableResolutionService;

    @GetMapping("/{functionId}/resolved-environment")
    public ResponseEntity<?> resolvedEnvironment(
            @PathVariable Long functionId
    ) {

        return variableResolutionService
                .resolveEnvironment(functionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound()
                                .build());
    }

    @GetMapping("/{functionId}/resolved-secrets")
    public ResponseEntity<?> resolvedSecrets(
            @PathVariable Long functionId
    ) {

        return variableResolutionService
                .resolveSecrets(functionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound()
                                .build());
    }
}