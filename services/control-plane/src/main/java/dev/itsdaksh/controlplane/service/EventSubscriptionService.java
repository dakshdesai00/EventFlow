package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventSubscriptionRequests.CreateEventSubscriptionRequest;
import dev.itsdaksh.controlplane.dto.EventSubscriptionRequests.EventSubscriptionResponse;
import dev.itsdaksh.controlplane.entity.EventSubscription;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.EventSubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventSubscriptionService {

    private final EventSubscriptionRepo eventSubscriptionRepo;
    private final EventService eventService;
    private final FunctionService functionService;
    private final CurrentUserService currentUserService;
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
        return eventService.getEventEntity(eventId)
                .flatMap(event ->
                        functionService.getFunctionEntity(
                                        request.functionId()
                                )
                                .map(function -> {
                                    EventSubscription subscription =
                                            EventSubscription.builder()
                                                    .event(event)
                                                    .function(function)
                                                    .build();
                                    subscription =
                                            eventSubscriptionRepo.save(
                                                    subscription
                                            );
                                    return map(subscription);
                                })
                );
    }
    public List<EventSubscriptionResponse> getEventSubscriptions(
            Long eventId
    ) {
        return eventService.getEventEntity(eventId)
                .map(event ->
                        eventSubscriptionRepo.findByEventId(eventId)
                                .stream()
                                .map(this::map)
                                .toList()
                )
                .orElse(List.of());
    }
    public Optional<EventSubscriptionResponse> deleteSubscription(
            Long subscriptionId
    ) {
        return getSubscriptionEntity(subscriptionId)
                .map(subscription -> {
                    eventSubscriptionRepo.delete(
                            subscription
                    );
                    return map(subscription);
                });
    }
    private Optional<EventSubscription>
    getSubscriptionEntity(
            Long subscriptionId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();
        return eventSubscriptionRepo
                .findByIdAndEventProjectUserId(
                        subscriptionId,
                        currentUser.getId()
                );
    }
    private EventSubscriptionResponse map(
            EventSubscription subscription
    ) {
        return new EventSubscriptionResponse(
                subscription.getId(),
                subscription.getEvent().getId(),
                subscription.getFunction().getId()
        );
    }

}