package dev.itsdaksh.controlplane.dto.ProjectRequests;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(

        @NotBlank
        String name,

        String description
) {}