package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventRequests.CreateEventRequest;
import dev.itsdaksh.controlplane.dto.EventRequests.EventResponse;
import dev.itsdaksh.controlplane.entity.Event;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.EventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepo eventRepo;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;
    public Optional<EventResponse> saveEvent(
            Long projectId,
            CreateEventRequest request
    ) {
        return projectService.getProjectById(projectId)
                .map(project -> {
                    String webhookToken =
                            Boolean.TRUE.equals(
                                    request.exposeWebhook()
                            )
                                    ? UUID.randomUUID().toString()
                                    : null;
                    Event event =
                            Event.builder()
                                    .name(request.name())
                                    .description(
                                            request.description()
                                    )
                                    .webhookToken(
                                            webhookToken
                                    )
                                    .project(project)
                                    .build();
                    event = eventRepo.save(event);
                    return map(event);
                });
    }
    public Optional<List<EventResponse>> getProjectEvents(
            Long projectId
    ) {
        return projectService.getProjectById(projectId)
                .map(project ->
                        eventRepo.findByProjectId(projectId)
                                .stream()
                                .map(this::map)
                                .toList()
                );
    }
    public Optional<EventResponse> getEvent(
            Long eventId
    ) {
        return getEventEntity(eventId)
                .map(this::map);
    }
    public Optional<EventResponse> updateEvent(
            Long eventId,
            CreateEventRequest request
    ) {
        return getEventEntity(eventId)
                .map(event -> {
                    event.setName(
                            request.name()
                    );
                    event.setDescription(
                            request.description()
                    );
                    Event updated =
                            eventRepo.save(event);
                    return map(updated);
                });
    }
    public Optional<EventResponse> deleteEvent(
            Long eventId
    ) {
        return getEventEntity(eventId)
                .map(event -> {
                    eventRepo.delete(event);
                    return map(event);
                });
    }
    public Optional<Event> getEventEntity(
            Long eventId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();
        return eventRepo.findByIdAndProjectUserId(
                eventId,
                currentUser.getId()
        );
    }
    private EventResponse map(
            Event event
    ) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getWebhookToken(),
                event.getProject().getId()
        );
    }

}