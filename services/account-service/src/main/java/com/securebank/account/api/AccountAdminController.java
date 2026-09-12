package com.securebank.account.api;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.application.AccountAdministrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AccountAdminController {

    private final AccountAdministrationService
        administrationService;

    public AccountAdminController(
        AccountAdministrationService
            administrationService
    ) {
        this.administrationService =
            administrationService;
    }

    @PatchMapping("/{accountId}/block")
    public AccountResponse block(
        @PathVariable UUID accountId
    ) {
        return administrationService.block(accountId);
    }

    @PatchMapping("/{accountId}/unblock")
    public AccountResponse unblock(
        @PathVariable UUID accountId
    ) {
        return administrationService.unblock(accountId);
    }
}
