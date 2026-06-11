package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.FunctionRequests.CreateFunctionRequest;
import dev.itsdaksh.controlplane.service.FunctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FunctionController {

    private final FunctionService functionService;

    @PostMapping("/api/projects/{projectId}/functions")
    public ResponseEntity<?> createFunction(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateFunctionRequest request
    ) {

        return functionService.createFunction(projectId, request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @GetMapping("/api/projects/{projectId}/functions")
    public ResponseEntity<?> getProjectFunctions(
            @PathVariable Long projectId
    ) {

        return functionService.getProjectFunctions(projectId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Project not found"));
    }

    @GetMapping("/api/functions/{functionId}")
    public ResponseEntity<?> getFunction(
            @PathVariable Long functionId
    ) {

        return functionService.getFunction(functionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @PutMapping("/api/functions/{functionId}")
    public ResponseEntity<?> updateFunction(
            @PathVariable Long functionId,
            @Valid @RequestBody CreateFunctionRequest request
    ) {

        return functionService.updateFunction(functionId, request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @DeleteMapping("/api/functions/{functionId}")
    public ResponseEntity<?> deleteFunction(
            @PathVariable Long functionId
    ) {

        return functionService.deleteFunction(functionId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok("Function deleted successfully"))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }
}