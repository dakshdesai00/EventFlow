package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests.CreateEventAllowedFunctionRequest;
import dev.itsdaksh.controlplane.dto.EventAllowedFunctionRequests.EventAllowedFunctionResponse;
import dev.itsdaksh.controlplane.entity.EventAllowedFunction;
import dev.itsdaksh.controlplane.repository.EventAllowedFunctionRepo;
import dev.itsdaksh.controlplane.repository.FunctionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventAllowedFunctionService {

    private final EventAllowedFunctionRepo repo;
    private final EventService eventService;
    private final FunctionRepo functionRepo;

    public Optional<EventAllowedFunctionResponse> create(
            Long eventId,
            CreateEventAllowedFunctionRequest request
    ) {

        return eventService.getEventEntity(eventId)
                .flatMap(event ->
                        functionRepo.findById(request.functionId())
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

        return repo.findById(id)
                .map(allowed -> {

                    repo.delete(allowed);

                    return map(allowed);
                });
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