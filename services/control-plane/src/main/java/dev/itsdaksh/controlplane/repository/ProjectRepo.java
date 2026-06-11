package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<Project, Long> {
}
