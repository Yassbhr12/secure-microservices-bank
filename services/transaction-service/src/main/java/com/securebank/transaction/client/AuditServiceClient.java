package com.securebank.transaction.client;

import com.securebank.transaction.client.dto.AuditEventRequest;
import com.securebank.transaction.domain.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AuditServiceClient {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            AuditServiceClient.class
        );

    private static final String AUDIT_PATH =
        "/api/v1/internal/audit-events";

    private final RestClient restClient;

    public AuditServiceClient(
        @Qualifier("auditServiceRestClient")
        RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public void recordTransferCompleted(
        Transfer transfer
    ) {
        send(
            AuditEventRequest.transferCompleted(
                transfer
            )
        );
    }

    public void recordTransferFailed(
        Transfer transfer
    ) {
        send(
            AuditEventRequest.transferFailed(
                transfer
            )
        );
    }

    private void send(AuditEventRequest request) {
        try {
            restClient
                .post()
                .uri(AUDIT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        } catch (RestClientException exception) {
            LOGGER.warn(
                "Audit delivery failed: action={}, "
                    + "resourceId={}, cause={}",
                request.action(),
                request.resourceId(),
                exception.getClass().getSimpleName()
            );
        }
    }
}
