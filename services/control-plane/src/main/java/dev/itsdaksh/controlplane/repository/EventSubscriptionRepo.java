package dev.itsdaksh.controlplane.repository;

import dev.itsdaksh.controlplane.entity.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventSubscriptionRepo
        extends JpaRepository<EventSubscription, Long> {

    List<EventSubscription> findByEventId(
            Long eventId
    );
    List<EventSubscription> findByFunctionId(
            Long functionId
    );
    boolean existsByEventIdAndFunctionId(
            Long eventId,
            Long functionId
    );
    Optional<EventSubscription>
    findByIdAndEventProjectUserId(
            Long subscriptionId,
            Long userId
    );

}