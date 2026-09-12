package com.securebank.account.api;

import com.securebank.account.api.dto.InternalTransferRequest;
import com.securebank.account.api.dto.InternalTransferResponse;
import com.securebank.account.application.AccountTransferResult;
import com.securebank.account.application.AccountTransferService;
import com.securebank.account.config.InternalApiKeyVerifier;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/transfers")
public class InternalTransferController {

    private static final String INTERNAL_KEY_HEADER =
        "X-Internal-Api-Key";

    private final AccountTransferService
        accountTransferService;
    private final InternalApiKeyVerifier apiKeyVerifier;

    public InternalTransferController(
        AccountTransferService accountTransferService,
        InternalApiKeyVerifier apiKeyVerifier
    ) {
        this.accountTransferService =
            accountTransferService;
        this.apiKeyVerifier = apiKeyVerifier;
    }

    @PostMapping
    public ResponseEntity<InternalTransferResponse>
    execute(
        @RequestHeader(
            value = INTERNAL_KEY_HEADER,
            required = false
        )
        String internalApiKey,

        @AuthenticationPrincipal
        Jwt jwt,

        @Valid
        @RequestBody
        InternalTransferRequest request
    ) {
        apiKeyVerifier.verify(internalApiKey);

        UUID ownerId = UUID.fromString(
            jwt.getSubject()
        );

        AccountTransferResult result =
            accountTransferService.execute(
                request.toCommand(ownerId)
            );

        return ResponseEntity.ok(
            InternalTransferResponse.from(result)
        );
    }
}
