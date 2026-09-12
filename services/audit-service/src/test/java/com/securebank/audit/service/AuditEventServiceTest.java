package com.securebank.audit.service;

import com.securebank.audit.domain.model.AuditEvent;
import com.securebank.audit.domain.model.AuditResult;
import com.securebank.audit.domain.model.AuditSeverity;
import com.securebank.audit.dto.AuditEventResponse;
import com.securebank.audit.dto.CreateAuditEventRequest;
import com.securebank.audit.exception.InvalidAuditSearchException;
import com.securebank.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    private static final Instant RECEIVED_AT =
        Instant.parse("2026-09-12T12:00:00Z");

    private static final Instant OCCURRED_AT =
        Instant.parse("2026-09-12T11:59:00Z");

    @Mock
    private AuditEventRepository auditEventRepository;

    private AuditEventService auditEventService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
            RECEIVED_AT,
            ZoneOffset.UTC
        );

        auditEventService = new AuditEventService(
            auditEventRepository,
            clock
        );
    }

    @Test
    void shouldRecordAuditEvent() {
        CreateAuditEventRequest request =
            new CreateAuditEventRequest(
                "transaction-service",
                "actor-123",
                "CLIENT",
                "TRANSFER_COMPLETED",
                "TRANSFER",
                "transfer-456",
                AuditResult.SUCCESS,
                AuditSeverity.INFO,
                "127.0.0.1",
                "correlation-789",
                "Transfer completed",
                OCCURRED_AT
            );

        when(
            auditEventRepository.save(
                any(AuditEvent.class)
            )
        ).thenAnswer(
            invocation ->
                (AuditEvent) invocation.getArgument(0)
        );

        AuditEventResponse response =
            auditEventService.record(request);

        ArgumentCaptor<AuditEvent> captor =
            ArgumentCaptor.forClass(
                AuditEvent.class
            );

        verify(auditEventRepository)
            .save(captor.capture());

        AuditEvent savedEvent = captor.getValue();

        assertNotNull(response.id());

        assertEquals(
            RECEIVED_AT,
            savedEvent.getReceivedAt()
        );

        assertEquals(
            OCCURRED_AT,
            savedEvent.getOccurredAt()
        );

        assertEquals(
            "TRANSFER_COMPLETED",
            savedEvent.getAction()
        );

        assertEquals(
            AuditResult.SUCCESS,
            response.result()
        );
    }

    @Test
    void shouldRejectInvertedDateRange() {
        Instant from =
            Instant.parse("2026-09-12T12:00:00Z");

        Instant to =
            Instant.parse("2026-09-11T12:00:00Z");

        assertThrows(
            InvalidAuditSearchException.class,
            () -> auditEventService.search(
                null,
                null,
                null,
                null,
                from,
                to,
                0,
                20
            )
        );

        verifyNoInteractions(auditEventRepository);
    }
}
