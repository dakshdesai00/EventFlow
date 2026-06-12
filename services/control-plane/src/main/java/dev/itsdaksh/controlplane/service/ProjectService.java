package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.ProjectRequests.CreateProjectRequest;
import dev.itsdaksh.controlplane.dto.ProjectRequests.ProjectResponse;
import dev.itsdaksh.controlplane.entity.Project;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepo projectRepo;
    private final CurrentUserService currentUserService;

    public Optional<ProjectResponse> createProject(
            CreateProjectRequest createProjectRequest
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        Project project =
                Project.builder()
                        .name(createProjectRequest.name())
                        .description(createProjectRequest.description())
                        .user(currentUser)
                        .build();

        project = projectRepo.save(project);

        return Optional.of(
                new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription()
                )
        );
    }

    public Optional<List<ProjectResponse>> getAllProjects() {

        User currentUser =
                currentUserService.getCurrentUser();

        List<Project> projects =
                projectRepo.findByUserId(
                        currentUser.getId()
                );

        if (projects.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                projects.stream()
                        .map(project ->
                                new ProjectResponse(
                                        project.getId(),
                                        project.getName(),
                                        project.getDescription()
                                )
                        )
                        .toList()
        );
    }

    public Optional<ProjectResponse> updateProject(
            Long projectId,
            CreateProjectRequest createProjectRequest
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return projectRepo
                .findByIdAndUserId(
                        projectId,
                        currentUser.getId()
                )
                .map(project -> {

                    project.setName(
                            createProjectRequest.name()
                    );

                    project.setDescription(
                            createProjectRequest.description()
                    );

                    Project updatedProject =
                            projectRepo.save(project);

                    return new ProjectResponse(
                            updatedProject.getId(),
                            updatedProject.getName(),
                            updatedProject.getDescription()
                    );
                });
    }

    public Optional<ProjectResponse> deleteProject(
            Long projectId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return projectRepo
                .findByIdAndUserId(
                        projectId,
                        currentUser.getId()
                )
                .map(project -> {

                    projectRepo.delete(project);

                    return new ProjectResponse(
                            project.getId(),
                            project.getName(),
                            project.getDescription()
                    );
                });
    }

    public Optional<Project> getProjectById(
            Long projectId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        return projectRepo.findByIdAndUserId(
                projectId,
                currentUser.getId()
        );
    }
}