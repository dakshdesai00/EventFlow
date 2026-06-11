package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableProjectRequest;
import dev.itsdaksh.controlplane.service.EnvironmentVariableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/environment-variables")
@RequiredArgsConstructor
public class EnvironmentVariableController {

    private final EnvironmentVariableService environmentVariableService;

    @PostMapping
    public ResponseEntity<?> createProjectEnvironmentVariable(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateEnvironmentVariableProjectRequest request
    ) {

        return environmentVariableService
                .saveProjectEnvironmentVariable(projectId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @GetMapping
    public ResponseEntity<?> getProjectEnvironmentVariables(
            @PathVariable Long projectId
    ) {

        return environmentVariableService
                .getProjectEnvironmentVariable(projectId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @PutMapping("/{envId}")
    public ResponseEntity<?> updateProjectEnvironmentVariable(
            @PathVariable Long projectId,
            @PathVariable Long envId,
            @Valid @RequestBody CreateEnvironmentVariableProjectRequest request
    ) {

        return environmentVariableService
                .updateProjectEnvironmentVariable(
                        projectId,
                        envId,
                        request
                )
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Environment variable not found"));
    }

    @DeleteMapping("/{envId}")
    public ResponseEntity<?> deleteProjectEnvironmentVariable(
            @PathVariable Long projectId,
            @PathVariable Long envId
    ) {

        return environmentVariableService
                .deleteProjectEnvironmentVariable(
                        projectId,
                        envId
                )
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Environment variable deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Environment variable not found"));
    }
}