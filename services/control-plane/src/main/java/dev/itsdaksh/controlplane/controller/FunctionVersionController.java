package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.service.FunctionVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FunctionVersionController {

    private final FunctionVersionService functionVersionService;

    @PostMapping(
            value = "/api/functions/{functionId}/versions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadVersion(
            @PathVariable Long functionId,
            @RequestParam("file") MultipartFile file
    ) {
        return functionVersionService
                .uploadVersion(functionId, file)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function not found"));
    }

    @GetMapping("/api/functions/{functionId}/versions")
    public ResponseEntity<?> getVersions(
            @PathVariable Long functionId
    ) {

        return ResponseEntity.ok(
                functionVersionService.getVersions(functionId)
        );
    }

    @GetMapping("/api/function-versions/{versionId}")
    public ResponseEntity<?> getVersion(
            @PathVariable Long versionId
    ) {

        return functionVersionService.getVersion(versionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Version not found"));
    }

    @GetMapping("/api/function-versions/{versionId}/code")
    public ResponseEntity<?> getCode(
            @PathVariable Long versionId
    ) {

        return functionVersionService.getCode(versionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Version not found"));
    }

    @PutMapping("/api/functions/{functionId}/active-version/{versionId}")
    public ResponseEntity<?> setActiveVersion(
            @PathVariable Long functionId,
            @PathVariable Long versionId
    ) {

        return functionVersionService
                .setActiveVersion(functionId, versionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Function or version not found"));
    }

    @DeleteMapping("/api/function-versions/{versionId}")
    public ResponseEntity<?> deleteVersion(
            @PathVariable Long versionId
    ) {

        return functionVersionService.deleteVersion(versionId)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.ok(
                                "Function version deleted successfully"
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(
                                        "Version not found or active version cannot be deleted"
                                ));
    }
}
