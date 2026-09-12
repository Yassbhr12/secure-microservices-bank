package com.securebank.transaction.api;

import com.securebank.transaction.api.dto.CreateTransferRequest;
import com.securebank.transaction.api.dto.TransferResponse;
import com.securebank.transaction.application.TransferExecutionService;
import com.securebank.transaction.application.TransferQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private static final String IDEMPOTENCY_HEADER =
        "Idempotency-Key";

    private final TransferExecutionService executionService;
    private final TransferQueryService queryService;

    public TransferController(
        TransferExecutionService executionService,
        TransferQueryService queryService
    ) {
        this.executionService = executionService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(
        @RequestHeader(
            value = IDEMPOTENCY_HEADER,
            required = false
        )
        String idempotencyKey,

        @AuthenticationPrincipal
        Jwt jwt,

        @Valid
        @RequestBody
        CreateTransferRequest request
    ) {
        UUID ownerId = ownerId(jwt);

        TransferResponse response =
            executionService.execute(
                ownerId,
                idempotencyKey,
                request,
                jwt.getTokenValue()
            );

        URI location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{transferId}")
                .buildAndExpand(
                    response.transferId()
                )
                .toUri();

        return ResponseEntity
            .created(location)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>>
    findAll(
        @AuthenticationPrincipal
        Jwt jwt
    ) {
        return ResponseEntity.ok(
            queryService.findAllForOwner(
                ownerId(jwt)
            )
        );
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponse> findOne(
        @PathVariable
        UUID transferId,

        @AuthenticationPrincipal
        Jwt jwt
    ) {
        return ResponseEntity.ok(
            queryService.findForOwner(
                transferId,
                ownerId(jwt)
            )
        );
    }

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(
            jwt.getSubject()
        );
    }
}
