package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests.CreateEventAllowedFunctionRequest;
import dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests.EventAllowedFunctionResponse;
import dev.itsdaksh.controlplane.entity.EventAllowedFunction;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.EventAllowedFunctionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventAllowedFunctionService {

    private final EventAllowedFunctionRepo repo;
    private final EventService eventService;
    private final FunctionService functionService;
    private final CurrentUserService currentUserService;
    public Optional<EventAllowedFunctionResponse> create(
            Long eventId,
            CreateEventAllowedFunctionRequest request
    ) {
        return eventService.getEventEntity(eventId)
                .flatMap(event ->
                        functionService.getFunctionEntity(
                                        request.functionId()
                                )
                                .map(function -> {
                                    EventAllowedFunction allowed =
                                            EventAllowedFunction.builder()
                                                    .event(event)
                                                    .function(function)
                                                    .build();
                                    allowed = repo.save(allowed);
                                    return map(allowed);
                                })
                );
    }
    public Optional<List<EventAllowedFunctionResponse>> getAll(
            Long eventId
    ) {
        return eventService.getEventEntity(eventId)
                .map(event ->
                        repo.findByEventId(eventId)
                                .stream()
                                .map(this::map)
                                .toList()
                );
    }
    public Optional<EventAllowedFunctionResponse> delete(
            Long id
    ) {
        return getAllowedEntity(id)
                .map(allowed -> {
                    repo.delete(allowed);
                    return map(allowed);
                });
    }
    private Optional<EventAllowedFunction>
    getAllowedEntity(
            Long id
    ) {
        User currentUser =
                currentUserService.getCurrentUser();
        return repo.findByIdAndEventProjectUserId(
                id,
                currentUser.getId()
        );
    }
    private EventAllowedFunctionResponse map(
            EventAllowedFunction allowed
    ) {
        return new EventAllowedFunctionResponse(
                allowed.getId(),
                allowed.getEvent().getId(),
                allowed.getFunction().getId()
        );
    }

}