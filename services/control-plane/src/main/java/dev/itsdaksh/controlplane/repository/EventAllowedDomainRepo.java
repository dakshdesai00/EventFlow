package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EventAllowedDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventAllowedDomainRepo
        extends JpaRepository<EventAllowedDomain, Long> {

    List<EventAllowedDomain> findByEventId(
            Long eventId
    );
    Optional<EventAllowedDomain>
    findByIdAndEventProjectUserId(
            Long domainId,
            Long userId
    );

}