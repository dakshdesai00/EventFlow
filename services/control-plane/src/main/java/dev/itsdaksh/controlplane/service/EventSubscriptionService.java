package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventSubscriptionRequests.CreateEventSubscriptionRequest;
import dev.itsdaksh.controlplane.dto.EventSubscriptionRequests.EventSubscriptionResponse;
import dev.itsdaksh.controlplane.entity.EventSubscription;
import dev.itsdaksh.controlplane.repository.EventRepo;
import dev.itsdaksh.controlplane.repository.EventSubscriptionRepo;
import dev.itsdaksh.controlplane.repository.FunctionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventSubscriptionService {

    private final EventSubscriptionRepo eventSubscriptionRepo;
    private final EventRepo eventRepo;
    private final FunctionRepo functionRepo;

    public Optional<EventSubscriptionResponse> createSubscription(
            Long eventId,
            CreateEventSubscriptionRequest request
    ) {

        if (eventSubscriptionRepo.existsByEventIdAndFunctionId(
                eventId,
                request.functionId()
        )) {
            return Optional.empty();
        }

        return eventRepo.findById(eventId)
                .flatMap(event ->
                        functionRepo.findById(request.functionId())
                                .map(function -> {

                                    EventSubscription subscription =
                                            EventSubscription.builder()
                                                    .event(event)
                                                    .function(function)
                                                    .build();

                                    subscription =
                                            eventSubscriptionRepo.save(subscription);

                                    return new EventSubscriptionResponse(
                                            subscription.getId(),
                                            event.getId(),
                                            function.getId()
                                    );
                                })
                );
    }

    public List<EventSubscriptionResponse> getEventSubscriptions(
            Long eventId
    ) {

        return eventSubscriptionRepo.findByEventId(eventId)
                .stream()
                .map(subscription ->
                        new EventSubscriptionResponse(
                                subscription.getId(),
                                subscription.getEvent().getId(),
                                subscription.getFunction().getId()
                        )
                )
                .toList();
    }

    public Optional<EventSubscriptionResponse> deleteSubscription(
            Long subscriptionId
    ) {

        return eventSubscriptionRepo.findById(subscriptionId)
                .map(subscription -> {

                    eventSubscriptionRepo.delete(subscription);

                    return new EventSubscriptionResponse(
                            subscription.getId(),
                            subscription.getEvent().getId(),
                            subscription.getFunction().getId()
                    );
                });
    }
}