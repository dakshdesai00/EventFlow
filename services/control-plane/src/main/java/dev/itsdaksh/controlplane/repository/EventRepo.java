package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepo extends JpaRepository<Event, Long> {
    List<Event> findByProjectId(Long projectId);
    Optional<Event> findByWebhookToken(
            String webhookToken
    );
}
