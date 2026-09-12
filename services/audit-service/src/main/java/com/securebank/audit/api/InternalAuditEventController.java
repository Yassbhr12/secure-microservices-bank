package com.securebank.audit.api;

import com.securebank.audit.config.InternalApiKeyVerifier;
import com.securebank.audit.dto.AuditEventResponse;
import com.securebank.audit.dto.CreateAuditEventRequest;
import com.securebank.audit.service.AuditEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/audit-events")
public class InternalAuditEventController {

    private static final String INTERNAL_KEY_HEADER =
        "X-Internal-Api-Key";

    private final AuditEventService auditEventService;
    private final InternalApiKeyVerifier apiKeyVerifier;

    public InternalAuditEventController(
        AuditEventService auditEventService,
        InternalApiKeyVerifier apiKeyVerifier
    ) {
        this.auditEventService = auditEventService;
        this.apiKeyVerifier = apiKeyVerifier;
    }

    @PostMapping
    public ResponseEntity<AuditEventResponse> record(
        @RequestHeader(
            value = INTERNAL_KEY_HEADER,
            required = false
        )
        String internalApiKey,

        @Valid
        @RequestBody
        CreateAuditEventRequest request
    ) {
        apiKeyVerifier.verify(internalApiKey);

        AuditEventResponse response =
            auditEventService.record(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}
