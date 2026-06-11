package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepo extends JpaRepository<Event, Long> {
    List<Event> findByProjectId(Long projectId);
}
