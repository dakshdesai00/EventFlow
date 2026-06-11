package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableProjectRequest;
import dev.itsdaksh.controlplane.service.SecretVariableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/secret-variables")
@RequiredArgsConstructor
public class SecretVariableController {

    private final SecretVariableService secretVariableService;

    @GetMapping
    public ResponseEntity<?> getSecretVariables(
            @PathVariable Long projectId
    ) {

        return secretVariableService
                .getProjectSecretVariable(projectId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @PostMapping
    public ResponseEntity<?> createSecretVariable(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSecretVariableProjectRequest request
    ) {

        return secretVariableService
                .saveProjectSecretVariable(projectId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @PutMapping("/{svId}")
    public ResponseEntity<?> updateSecretVariable(
            @PathVariable Long projectId,
            @PathVariable Long svId,
            @Valid @RequestBody CreateSecretVariableProjectRequest request
    ) {

        return secretVariableService
                .updateProjectSecretVariable(
                        projectId,
                        svId,
                        request
                )
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Secret variable not found"));
    }

    @DeleteMapping("/{svId}")
    public ResponseEntity<?> deleteSecretVariable(
            @PathVariable Long projectId,
            @PathVariable Long svId
    ) {

        return secretVariableService
                .deleteProjectSecretVariable(projectId, svId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Secret variable deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Secret variable not found"));
    }
}