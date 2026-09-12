package com.securebank.account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private UUID id;

    @Column(
        name = "owner_id",
        nullable = false,
        updatable = false
    )
    private UUID ownerId;

    @Column(
        name = "account_number",
        nullable = false,
        unique = true,
        length = 34,
        updatable = false
    )
    private String accountNumber;

    @Column(
        name = "balance",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal balance;

    @Column(
        name = "currency",
        nullable = false,
        length = 3,
        updatable = false
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 16
    )
    private AccountStatus status;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    @Version
    @Column(
        name = "version",
        nullable = false
    )
    private long version;

    protected Account() {
        // Constructeur exigé par JPA.
    }

    private Account(
        UUID ownerId,
        String accountNumber,
        String currency,
        Instant now
    ) {
        this.id = UUID.randomUUID();
        this.ownerId = Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );
        this.accountNumber = normalizeAccountNumber(
            accountNumber
        );
        this.balance = BigDecimal.ZERO.setScale(2);
        this.currency = normalizeCurrency(currency);
        this.status = AccountStatus.ACTIVE;
        this.createdAt = requireInstant(now);
        this.updatedAt = now;
    }

    public static Account open(
        UUID ownerId,
        String accountNumber,
        String currency,
        Instant now
    ) {
        return new Account(
            ownerId,
            accountNumber,
            currency,
            now
        );
    }

    public void block(Instant now) {
        if (status == AccountStatus.BLOCKED) {
            return;
        }

        status = AccountStatus.BLOCKED;
        updatedAt = requireInstant(now);
    }

    public void unblock(Instant now) {
        if (status == AccountStatus.ACTIVE) {
            return;
        }

        status = AccountStatus.ACTIVE;
        updatedAt = requireInstant(now);
    }

    public boolean isBlocked() {
        return status == AccountStatus.BLOCKED;
    }

    private static String normalizeAccountNumber(
        String accountNumber
    ) {
        if (accountNumber == null
            || accountNumber.isBlank()) {

            throw new IllegalArgumentException(
                "accountNumber is required"
            );
        }

        String normalized = accountNumber
            .trim()
            .toUpperCase(Locale.ROOT);

        if (normalized.length() > 34) {
            throw new IllegalArgumentException(
                "accountNumber must not exceed 34 characters"
            );
        }

        return normalized;
    }

    private static String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                "currency is required"
            );
        }

        String normalized = currency
            .trim()
            .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                "currency must contain exactly 3 letters"
            );
        }

        return normalized;
    }

    private static Instant requireInstant(Instant instant) {
        return Objects.requireNonNull(
            instant,
            "instant is required"
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Account other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
