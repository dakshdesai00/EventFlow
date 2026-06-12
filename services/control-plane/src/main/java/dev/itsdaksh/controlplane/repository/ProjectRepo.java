package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndUserId(
            Long projectId,
            Long userId
    );

    List<Project> findByUserId(
            Long userId
    );
}