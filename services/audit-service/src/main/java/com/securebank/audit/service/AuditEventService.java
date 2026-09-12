package com.securebank.audit.service;

import com.securebank.audit.domain.model.AuditEvent;
import com.securebank.audit.domain.model.AuditSeverity;
import com.securebank.audit.dto.AuditEventResponse;
import com.securebank.audit.dto.CreateAuditEventRequest;
import com.securebank.audit.dto.PagedResponse;
import com.securebank.audit.exception.AuditEventNotFoundException;
import com.securebank.audit.exception.InvalidAuditSearchException;
import com.securebank.audit.repository.AuditEventRepository;
import com.securebank.audit.repository.AuditEventSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final Clock clock;

    public AuditEventService(
        AuditEventRepository auditEventRepository,
        Clock clock
    ) {
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
    }

    @Transactional
    public AuditEventResponse record(
        CreateAuditEventRequest request
    ) {
        Objects.requireNonNull(
            request,
            "request is required"
        );

        Instant receivedAt = Instant.now(clock);

        AuditEvent event = AuditEvent.record(
            request.sourceService(),
            request.actorId(),
            request.actorRole(),
            request.action(),
            request.resourceType(),
            request.resourceId(),
            request.result(),
            request.severity(),
            request.ipAddress(),
            request.correlationId(),
            request.details(),
            request.occurredAt(),
            receivedAt
        );

        AuditEvent savedEvent =
            auditEventRepository.save(event);

        return AuditEventResponse.from(savedEvent);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditEventResponse> search(
        String actorId,
        String action,
        String sourceService,
        AuditSeverity severity,
        Instant from,
        Instant to,
        int page,
        int size
    ) {
        if (from != null
            && to != null
            && from.isAfter(to)) {

            throw new InvalidAuditSearchException(
                "'from' must be before or equal to 'to'"
            );
        }

        Specification<AuditEvent> specification =
            AuditEventSpecifications.withFilters(
                actorId,
                action,
                sourceService,
                severity,
                from,
                to
            );

        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Direction.DESC,
                "occurredAt"
            )
        );

        Page<AuditEventResponse> result =
            auditEventRepository
                .findAll(specification, pageRequest)
                .map(AuditEventResponse::from);

        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AuditEventResponse findById(UUID eventId) {
        AuditEvent event = auditEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new AuditEventNotFoundException(
                    eventId
                )
            );

        return AuditEventResponse.from(event);
    }
}
