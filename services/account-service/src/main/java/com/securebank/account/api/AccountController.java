package com.securebank.account.api;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.application.AccountCreationService;
import com.securebank.account.application.AccountQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@PreAuthorize("hasRole('CLIENT')")
public class AccountController {

    private final AccountCreationService
        accountCreationService;

    private final AccountQueryService
        accountQueryService;

    public AccountController(
        AccountCreationService accountCreationService,
        AccountQueryService accountQueryService
    ) {
        this.accountCreationService =
            accountCreationService;

        this.accountQueryService =
            accountQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return accountCreationService.createFor(
            extractOwnerId(jwt)
        );
    }

    @GetMapping
    public List<AccountResponse> findMyAccounts(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return accountQueryService.findAllForOwner(
            extractOwnerId(jwt)
        );
    }

    @GetMapping("/{accountId}")
    public AccountResponse findMyAccount(
        @PathVariable UUID accountId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return accountQueryService.findOneForOwner(
            accountId,
            extractOwnerId(jwt)
        );
    }

    private UUID extractOwnerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
