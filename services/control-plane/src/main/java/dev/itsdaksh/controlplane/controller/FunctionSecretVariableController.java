package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.SecretVariableRequests.CreateSecretVariableFunctionRequest;
import dev.itsdaksh.controlplane.service.SecretVariableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/functions/{functionId}/secret-variables")
@RequiredArgsConstructor
public class FunctionSecretVariableController {

    private final SecretVariableService secretVariableService;

    @GetMapping
    public ResponseEntity<?> getFunctionSecretVariables(
            @PathVariable Long functionId
    ) {

        return secretVariableService
                .getFunctionSecretVariable(functionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @PostMapping
    public ResponseEntity<?> createFunctionSecretVariable(
            @PathVariable Long functionId,
            @Valid @RequestBody CreateSecretVariableFunctionRequest request
    ) {

        return secretVariableService
                .saveFunctionSecretVariable(functionId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @PutMapping("/{svId}")
    public ResponseEntity<?> updateFunctionSecretVariable(
            @PathVariable Long functionId,
            @PathVariable Long svId,
            @Valid @RequestBody CreateSecretVariableFunctionRequest request
    ) {

        return secretVariableService
                .updateFunctionSecretVariable(
                        functionId,
                        svId,
                        request
                )
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Secret variable not found"));
    }

    @DeleteMapping("/{svId}")
    public ResponseEntity<?> deleteFunctionSecretVariable(
            @PathVariable Long functionId,
            @PathVariable Long svId
    ) {

        return secretVariableService
                .deleteFunctionSecretVariable(
                        functionId,
                        svId
                )
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Secret variable deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Secret variable not found"));
    }
}