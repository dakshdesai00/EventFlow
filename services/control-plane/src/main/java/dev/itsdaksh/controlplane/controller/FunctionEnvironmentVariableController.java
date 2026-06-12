package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.EnvironmentVariableRequests.CreateEnvironmentVariableFunctionRequest;
import dev.itsdaksh.controlplane.service.EnvironmentVariableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/functions/{functionId}/environment-variables")
@RequiredArgsConstructor
public class FunctionEnvironmentVariableController {

    private final EnvironmentVariableService environmentVariableService;

    @PostMapping
    public ResponseEntity<?> createFunctionEnvironmentVariable(
            @PathVariable Long functionId,
            @Valid @RequestBody CreateEnvironmentVariableFunctionRequest request
    ) {

        return environmentVariableService
                .saveFunctionEnvironmentVariable(functionId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @GetMapping
    public ResponseEntity<?> getFunctionEnvironmentVariables(
            @PathVariable Long functionId
    ) {

        return environmentVariableService
                .getFunctionEnvironmentVariables(functionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @PutMapping("/{envId}")
    public ResponseEntity<?> updateFunctionEnvironmentVariable(
            @PathVariable Long functionId,
            @PathVariable Long envId,
            @Valid @RequestBody CreateEnvironmentVariableFunctionRequest request
    ) {

        return environmentVariableService
                .updateFunctionEnvironmentVariable(
                        functionId,
                        envId,
                        request
                )
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Environment variable not found"));
    }

    @DeleteMapping("/{envId}")
    public ResponseEntity<?> deleteFunctionEnvironmentVariable(
            @PathVariable Long functionId,
            @PathVariable Long envId
    ) {

        return environmentVariableService
                .deleteFunctionEnvironmentVariable(
                        functionId,
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