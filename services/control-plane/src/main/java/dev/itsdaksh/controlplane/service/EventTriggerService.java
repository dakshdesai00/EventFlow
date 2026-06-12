package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventRequests.TriggerEventResponse;
import dev.itsdaksh.controlplane.dto.EventRequests.TriggeredFunctionResponse;
import dev.itsdaksh.controlplane.entity.Event;
import dev.itsdaksh.controlplane.repository.EventAllowedDomainRepo;
import dev.itsdaksh.controlplane.repository.EventRepo;
import dev.itsdaksh.controlplane.repository.EventSubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventTriggerService {

    private final EventRepo eventRepo;

    private final EventSubscriptionRepo eventSubscriptionRepo;

    private final EventAllowedDomainRepo eventAllowedDomainRepo;

    public Optional<TriggerEventResponse> triggerEvent(
            String token,
            String origin,
            Map<String, Object> payload
    ) {

        return eventRepo.findByWebhookToken(token)
                .filter(event -> validateOrigin(event, origin))
                .map(event -> {

                    List<TriggeredFunctionResponse> functions =
                            eventSubscriptionRepo
                                    .findByEventId(event.getId())
                                    .stream()
                                    .map(subscription ->
                                            new TriggeredFunctionResponse(
                                                    subscription.getFunction().getId(),
                                                    subscription.getFunction().getName()
                                            )
                                    )
                                    .toList();

                    return new TriggerEventResponse(
                            event.getId(),
                            event.getName(),
                            payload,
                            functions
                    );
                });
    }

    private boolean validateOrigin(
            Event event,
            String origin
    ) {

        if (origin == null || origin.isBlank()) {
            return true;
        }

        return eventAllowedDomainRepo
                .findByEventId(event.getId())
                .stream()
                .anyMatch(domain ->
                        origin.contains(
                                domain.getDomain()
                        )
                );
    }
}