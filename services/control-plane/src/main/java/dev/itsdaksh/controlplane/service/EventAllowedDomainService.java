package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.EventAllowedDomainRequests.CreateEventAllowedDomainRequest;
import dev.itsdaksh.controlplane.dto.EventAllowedDomainRequests.EventAllowedDomainResponse;
import dev.itsdaksh.controlplane.entity.EventAllowedDomain;
import dev.itsdaksh.controlplane.repository.EventAllowedDomainRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventAllowedDomainService {

    private final EventAllowedDomainRepo repo;
    private final EventService eventService;

    public Optional<EventAllowedDomainResponse> create(
            Long eventId,
            CreateEventAllowedDomainRequest request
    ) {

        return eventService.getEventEntity(eventId)
                .map(event -> {

                    EventAllowedDomain domain =
                            EventAllowedDomain.builder()
                                    .event(event)
                                    .domain(request.domain())
                                    .build();

                    domain = repo.save(domain);

                    return map(domain);
                });
    }

    public Optional<List<EventAllowedDomainResponse>> getAll(
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

    public Optional<EventAllowedDomainResponse> delete(
            Long domainId
    ) {

        return repo.findById(domainId)
                .map(domain -> {

                    repo.delete(domain);

                    return map(domain);
                });
    }

    private EventAllowedDomainResponse map(
            EventAllowedDomain domain
    ) {

        return new EventAllowedDomainResponse(
                domain.getId(),
                domain.getEvent().getId(),
                domain.getDomain()
        );
    }
}