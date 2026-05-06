package com.neobankx.account.api;

import com.neobankx.account.application.AccountService;
import com.neobankx.account.application.IdempotencyService;
import com.neobankx.account.application.PrincipalAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    private final IdempotencyService idempotencyService;

    public AccountController(AccountService accountService, IdempotencyService idempotencyService) {
        this.accountService = accountService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    AccountResponse createAccount(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PrincipalAccess principal = PrincipalAccess.from(jwt);
        return idempotencyService.execute(
                idempotencyKey,
                "create-account",
                request,
                AccountResponse.class,
                () -> accountService.createAccount(request, principal)
        );
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPPORT','ADMIN')")
    AccountResponse getAccount(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        return accountService.getAccount(accountId, PrincipalAccess.from(jwt));
    }

    @PostMapping("/{accountId}/freeze")
    @PreAuthorize("hasAnyRole('SUPPORT','ADMIN')")
    AccountResponse freezeAccount(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        return accountService.freezeAccount(accountId, PrincipalAccess.from(jwt));
    }

    @PostMapping("/{accountId}/activate")
    @PreAuthorize("hasAnyRole('SUPPORT','ADMIN')")
    AccountResponse activateAccount(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        return accountService.activateAccount(accountId, PrincipalAccess.from(jwt));
    }

    @GetMapping("/{accountId}/balances")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPPORT','ADMIN')")
    BalanceResponse getBalance(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        return accountService.getBalance(accountId, PrincipalAccess.from(jwt));
    }

    @GetMapping("/{accountId}/ledger")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPPORT','ADMIN')")
    List<LedgerEntryResponse> getLedger(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accountService.getLedger(accountId, limit, PrincipalAccess.from(jwt));
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    TransferResponse transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PrincipalAccess principal = PrincipalAccess.from(jwt);
        return idempotencyService.execute(
                idempotencyKey,
                "transfer",
                request,
                TransferResponse.class,
                () -> accountService.transfer(request, principal)
        );
    }
}

