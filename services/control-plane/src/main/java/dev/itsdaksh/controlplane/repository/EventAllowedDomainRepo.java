package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EventAllowedDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventAllowedDomainRepo
        extends JpaRepository<EventAllowedDomain, Long> {

    List<EventAllowedDomain> findByEventId(Long eventId);
}