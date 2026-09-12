package com.securebank.audit.api;

import com.securebank.audit.domain.model.AuditSeverity;
import com.securebank.audit.dto.AuditEventResponse;
import com.securebank.audit.dto.PagedResponse;
import com.securebank.audit.service.AuditEventService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(
        AuditEventService auditEventService
    ) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public PagedResponse<AuditEventResponse> search(
        @RequestParam(required = false)
        @Size(max = 128)
        String actorId,

        @RequestParam(required = false)
        @Size(max = 100)
        @Pattern(
            regexp = "^[A-Za-z0-9_]+$",
            message = "action has an invalid format"
        )
        String action,

        @RequestParam(required = false)
        @Size(max = 64)
        @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "sourceService has an invalid format"
        )
        String sourceService,

        @RequestParam(required = false)
        AuditSeverity severity,

        @RequestParam(required = false)
        @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        )
        Instant from,

        @RequestParam(required = false)
        @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        )
        Instant to,

        @RequestParam(defaultValue = "0")
        @Min(0)
        int page,

        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100)
        int size
    ) {
        return auditEventService.search(
            actorId,
            action,
            sourceService,
            severity,
            from,
            to,
            page,
            size
        );
    }

    @GetMapping("/{eventId}")
    public AuditEventResponse findById(
        @PathVariable UUID eventId
    ) {
        return auditEventService.findById(
            eventId
        );
    }
}
