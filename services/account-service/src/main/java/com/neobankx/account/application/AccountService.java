package com.neobankx.account.application;

import com.neobankx.account.api.AccountResponse;
import com.neobankx.account.api.BalanceResponse;
import com.neobankx.account.api.CreateAccountRequest;
import com.neobankx.account.api.LedgerEntryResponse;
import com.neobankx.account.api.TransferRequest;
import com.neobankx.account.api.TransferResponse;
import com.neobankx.account.config.AccountProperties;
import com.neobankx.account.domain.AccountProductType;
import com.neobankx.account.domain.LedgerEntryType;
import com.neobankx.account.domain.Money;
import com.neobankx.account.infrastructure.persistence.AccountEntity;
import com.neobankx.account.infrastructure.persistence.AccountRepository;
import com.neobankx.account.infrastructure.persistence.BalanceSnapshotEntity;
import com.neobankx.account.infrastructure.persistence.BalanceSnapshotRepository;
import com.neobankx.account.infrastructure.persistence.LedgerEntryEntity;
import com.neobankx.account.infrastructure.persistence.LedgerEntryRepository;
import com.neobankx.common.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accounts;
    private final BalanceSnapshotRepository balances;
    private final LedgerEntryRepository ledgerEntries;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountEventPublisher events;
    private final AccountProperties properties;
    private final Clock clock;

    public AccountService(
            AccountRepository accounts,
            BalanceSnapshotRepository balances,
            LedgerEntryRepository ledgerEntries,
            AccountNumberGenerator accountNumberGenerator,
            AccountEventPublisher events,
            AccountProperties properties,
            Clock clock
    ) {
        this.accounts = accounts;
        this.balances = balances;
        this.ledgerEntries = ledgerEntries;
        this.accountNumberGenerator = accountNumberGenerator;
        this.events = events;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, PrincipalAccess principal) {
        String currency = request.currency() == null ? properties.defaultCurrency() : request.currency();
        AccountProductType productType = request.productType() == null ? AccountProductType.CHECKING : request.productType();
        if (productType == AccountProductType.SYSTEM && !principal.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SYSTEM_ACCOUNT_FORBIDDEN", "System accounts require administrator access");
        }
        String ownerSubject = resolveOwnerSubject(request.ownerSubject(), principal);
        Money initialDeposit = new Money(request.initialDeposit() == null ? BigDecimal.ZERO : request.initialDeposit(), currency);
        initialDeposit.requireNonNegative();

        Instant now = clock.instant();
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                uniqueAccountNumber(),
                ownerSubject,
                currency,
                productType,
                now
        );
        accounts.save(account);
        balances.save(new BalanceSnapshotEntity(account.getId(), currency, initialDeposit.amount(), now));

        if (initialDeposit.amount().signum() > 0) {
            AccountEntity systemAccount = accounts.findById(properties.systemOpeningAccountId())
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_ACCOUNT_MISSING", "Opening offset account is not configured"));
            BalanceSnapshotEntity systemBalance = balances.findById(systemAccount.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_BALANCE_MISSING", "Opening offset balance is not configured"));
            if (!systemBalance.getCurrency().equals(currency)) {
                throw new ApiException(HttpStatus.CONFLICT, "SYSTEM_CURRENCY_MISMATCH", "Opening offset account currency does not match");
            }
            UUID entryGroupId = UUID.randomUUID();
            BigDecimal offset = initialDeposit.amount().negate();
            assertBalanced(List.of(offset, initialDeposit.amount()));
            ledgerEntries.save(new LedgerEntryEntity(systemAccount.getId(), entryGroupId, offset, currency, LedgerEntryType.ACCOUNT_OPENING, "Opening funding offset", now));
            ledgerEntries.save(new LedgerEntryEntity(account.getId(), entryGroupId, initialDeposit.amount(), currency, LedgerEntryType.ACCOUNT_OPENING, "Opening balance", now));
            systemBalance.apply(offset, now);
        }

        AccountResponse response = toAccountResponse(account);
        events.publish("account-created", account.getId(), Map.of("ownerSubject", ownerSubject, "currency", currency, "productType", productType.name()));
        if (initialDeposit.amount().signum() > 0) {
            events.publish("balance-updated", account.getId(), Map.of("ledgerBalance", initialDeposit.amount(), "currency", currency));
        }
        log.info("account_created accountId={} ownerSubject={} currency={}", account.getId(), ownerSubject, currency);
        return response;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId, PrincipalAccess principal) {
        AccountEntity account = findAccount(accountId);
        requireAccountAccess(account, principal);
        return toAccountResponse(account);
    }

    @Transactional
    public AccountResponse freezeAccount(UUID accountId, PrincipalAccess principal) {
        requirePrivileged(principal);
        AccountEntity account = findAccount(accountId);
        account.freeze(clock.instant());
        AccountResponse response = toAccountResponse(account);
        events.publish("account-frozen", account.getId(), Map.of("status", account.getStatus().name()));
        log.info("account_frozen accountId={} actor={}", accountId, principal.subject());
        return response;
    }

    @Transactional
    public AccountResponse activateAccount(UUID accountId, PrincipalAccess principal) {
        requirePrivileged(principal);
        AccountEntity account = findAccount(accountId);
        account.activate(clock.instant());
        AccountResponse response = toAccountResponse(account);
        events.publish("account-activated", account.getId(), Map.of("status", account.getStatus().name()));
        log.info("account_activated accountId={} actor={}", accountId, principal.subject());
        return response;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID accountId, PrincipalAccess principal) {
        AccountEntity account = findAccount(accountId);
        requireAccountAccess(account, principal);
        return toBalanceResponse(findBalance(accountId));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedger(UUID accountId, int limit, PrincipalAccess principal) {
        AccountEntity account = findAccount(accountId);
        requireAccountAccess(account, principal);
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return ledgerEntries.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, boundedLimit))
                .stream()
                .map(this::toLedgerResponse)
                .toList();
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, PrincipalAccess principal) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSFER_SAME_ACCOUNT", "Transfer accounts must be different");
        }
        Money money = new Money(request.amount(), request.currency());
        money.requirePositive();

        List<UUID> orderedIds = List.of(request.fromAccountId(), request.toAccountId()).stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        AccountEntity first = findAccount(orderedIds.get(0));
        AccountEntity second = findAccount(orderedIds.get(1));
        AccountEntity from = first.getId().equals(request.fromAccountId()) ? first : second;
        AccountEntity to = first.getId().equals(request.toAccountId()) ? first : second;
        requireAccountAccess(from, principal);
        from.requireActive();
        to.requireActive();
        if (!from.getCurrency().equals(money.currency()) || !to.getCurrency().equals(money.currency())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CURRENCY_MISMATCH", "Transfer currency must match both accounts");
        }

        BalanceSnapshotEntity fromBalance = findBalance(from.getId());
        BalanceSnapshotEntity toBalance = findBalance(to.getId());
        if (fromBalance.getAvailableBalance().compareTo(money.amount()) < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS", "Available balance is insufficient");
        }

        Instant now = clock.instant();
        UUID transferId = UUID.randomUUID();
        BigDecimal debit = money.amount().negate();
        BigDecimal credit = money.amount();
        assertBalanced(List.of(debit, credit));
        fromBalance.apply(debit, now);
        toBalance.apply(credit, now);
        ledgerEntries.save(new LedgerEntryEntity(from.getId(), transferId, debit, money.currency(), LedgerEntryType.TRANSFER_DEBIT, request.memo(), now));
        ledgerEntries.save(new LedgerEntryEntity(to.getId(), transferId, credit, money.currency(), LedgerEntryType.TRANSFER_CREDIT, request.memo(), now));
        events.publish("balance-updated", from.getId(), Map.of("ledgerBalance", fromBalance.getLedgerBalance(), "currency", money.currency()));
        events.publish("balance-updated", to.getId(), Map.of("ledgerBalance", toBalance.getLedgerBalance(), "currency", money.currency()));
        log.info("transfer_posted transferId={} fromAccountId={} toAccountId={} amount={} currency={}",
                transferId, from.getId(), to.getId(), money.amount(), money.currency());
        return new TransferResponse(transferId, from.getId(), to.getId(), money.amount(), money.currency(), now);
    }

    private String resolveOwnerSubject(String requestedOwner, PrincipalAccess principal) {
        if (principal.isAdmin() && requestedOwner != null && !requestedOwner.isBlank()) {
            return requestedOwner.trim();
        }
        return principal.subject();
    }

    private void requirePrivileged(PrincipalAccess principal) {
        if (!principal.isPrivileged()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_ACCESS_DENIED", "Privileged account access is required");
        }
    }

    private void requireAccountAccess(AccountEntity account, PrincipalAccess principal) {
        if (!principal.isPrivileged() && !account.isOwnedBy(principal.subject())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_ACCESS_DENIED", "Account access is denied");
        }
    }

    private AccountEntity findAccount(UUID accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account was not found"));
    }

    private BalanceSnapshotEntity findBalance(UUID accountId) {
        return balances.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BALANCE_NOT_FOUND", "Account balance was not found"));
    }

    private String uniqueAccountNumber() {
        String accountNumber = accountNumberGenerator.generate();
        while (accounts.existsByAccountNumber(accountNumber)) {
            accountNumber = accountNumberGenerator.generate();
        }
        return accountNumber;
    }

    private void assertBalanced(List<BigDecimal> postings) {
        BigDecimal total = postings.stream().reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO.setScale(2)) != 0) {
            throw new IllegalStateException("Ledger posting group is not balanced");
        }
    }

    private AccountResponse toAccountResponse(AccountEntity account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerSubject(),
                account.getCurrency(),
                account.getProductType(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getVersion()
        );
    }

    private BalanceResponse toBalanceResponse(BalanceSnapshotEntity balance) {
        return new BalanceResponse(
                balance.getAccountId(),
                balance.getCurrency(),
                balance.getLedgerBalance(),
                balance.getAvailableBalance(),
                balance.getUpdatedAt(),
                balance.getVersion()
        );
    }

    private LedgerEntryResponse toLedgerResponse(LedgerEntryEntity entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getAccountId(),
                entry.getEntryGroupId(),
                entry.getSignedAmount(),
                entry.getCurrency(),
                entry.getEntryType(),
                entry.getMemo(),
                entry.getCreatedAt()
        );
    }
}
