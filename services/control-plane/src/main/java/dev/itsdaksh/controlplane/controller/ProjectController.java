package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.ProjectRequests.CreateProjectRequest;
import dev.itsdaksh.controlplane.dto.ProjectRequests.ProjectResponse;
import dev.itsdaksh.controlplane.service.ProjectService;
import dev.itsdaksh.controlplane.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final StorageService storageService;
    @GetMapping
    public ResponseEntity<?> getProjects() {
        return projectService.getAllProjects()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("No projects found"));
    }



    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.createProject(request)
                .<ResponseEntity<?>>map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to create project"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody CreateProjectRequest request) {
        return projectService.updateProject(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProjectResponse> deleteProject(@PathVariable Long id) {
        return projectService.deleteProject(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

