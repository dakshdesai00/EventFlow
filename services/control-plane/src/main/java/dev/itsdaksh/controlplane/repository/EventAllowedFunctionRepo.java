package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EventAllowedFunction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventAllowedFunctionRepo
        extends JpaRepository<EventAllowedFunction, Long> {

    List<EventAllowedFunction> findByEventId(
            Long eventId
    );
    Optional<EventAllowedFunction>
    findByIdAndEventProjectUserId(
            Long id,
            Long userId
    );

}