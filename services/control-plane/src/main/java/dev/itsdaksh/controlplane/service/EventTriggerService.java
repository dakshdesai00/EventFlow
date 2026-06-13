package dev.itsdaksh.controlplane.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.itsdaksh.controlplane.dto.EventRequests.TriggerEventResponse;
import dev.itsdaksh.controlplane.dto.EventRequests.TriggeredFunctionResponse;
import dev.itsdaksh.controlplane.entity.Event;
import dev.itsdaksh.controlplane.entity.EventAllowedDomain;
import dev.itsdaksh.controlplane.entity.Execution;
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
    private final ObjectMapper objectMapper;
    private final EventRepo eventRepo;

    private final EventSubscriptionRepo eventSubscriptionRepo;

    private final EventAllowedDomainRepo eventAllowedDomainRepo;

    private final ExecutionService executionService;

    private final ExecutionProducerService executionProducerService;

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
                                    .map(subscription -> {

                                        String payloadJson;

                                        try {
                                            payloadJson =
                                                    objectMapper.writeValueAsString(
                                                            payload
                                                    );
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }

                                        Execution execution =
                                                executionService.createExecution(
                                                        event,
                                                        subscription.getFunction(),
                                                        payloadJson
                                                );

                                        executionProducerService.publishExecution(
                                                execution.getId()
                                        );

                                        return new TriggeredFunctionResponse(
                                                execution.getId(),
                                                subscription.getFunction().getName()
                                        );
                                    })
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

        List<String> allowedDomains =
                eventAllowedDomainRepo
                        .findByEventId(event.getId())
                        .stream()
                        .map(EventAllowedDomain::getDomain)
                        .toList();

        if (allowedDomains.isEmpty()) {
            return true;
        }

        if (origin == null || origin.isBlank()) {
            return false;
        }

        return allowedDomains.stream()
                .anyMatch(origin::contains);
    }
}