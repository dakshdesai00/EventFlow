package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.ProjectRequests.CreateProjectRequest;
import dev.itsdaksh.controlplane.dto.ProjectRequests.ProjectResponse;
import dev.itsdaksh.controlplane.entity.Project;
import dev.itsdaksh.controlplane.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;
    public Optional<ProjectResponse> createProject(CreateProjectRequest createProjectRequest) {
        Project project = Project.builder()
                .name(createProjectRequest.name())
                .description(createProjectRequest.description())
                .build();
        project = projectRepo.save(project);
        return Optional.of(new ProjectResponse(project.getId(), project.getName(), project.getDescription()));
    }

    public Optional<List<ProjectResponse>> getAllProjects() {
        List<Project> projects = projectRepo.findAll();
        if (projects.isEmpty()) return Optional.empty();
        List<ProjectResponse> projectResponses = new ArrayList<>();
        projects.forEach(project -> projectResponses.add(new ProjectResponse(project.getId(), project.getName(), project.getDescription())));
        return Optional.of(projectResponses);
    }

    public Optional<ProjectResponse> updateProject(Long projectId, CreateProjectRequest createProjectRequest) {
        return projectRepo.findById(projectId)
                .map(project -> {
                    project.setName(createProjectRequest.name());
                    project.setDescription(createProjectRequest.description());
                    Project updatedProject = projectRepo.save(project);
                    return new ProjectResponse(updatedProject.getId(), updatedProject.getName(), updatedProject.getDescription());
                })
                .or(Optional::empty);
    }

    public Optional<ProjectResponse> deleteProject(Long projectId) {
        return projectRepo.findById(projectId)
                .map(project -> {
                    projectRepo.delete(project);
                    return new ProjectResponse(project.getId(), project.getName(), project.getDescription());
                })
                .or(Optional::empty);
    }

    public Optional<Project> getProjectById(Long projectId) {
        return projectRepo.findById(projectId);
    }

}
