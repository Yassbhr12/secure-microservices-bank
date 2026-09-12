package com.securebank.transaction.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
        name = "owner_id",
        nullable = false,
        updatable = false
    )
    private UUID ownerId;

    @Column(
        name = "source_account_id",
        nullable = false,
        updatable = false
    )
    private UUID sourceAccountId;

    @Column(
        name = "destination_account_id",
        nullable = false,
        updatable = false
    )
    private UUID destinationAccountId;

    @Column(
        nullable = false,
        precision = 19,
        scale = 2,
        updatable = false
    )
    private BigDecimal amount;

    @Column(
        nullable = false,
        length = 3,
        updatable = false
    )
    private String currency;

    @Column(
        name = "idempotency_key",
        nullable = false,
        length = 128,
        updatable = false
    )
    private String idempotencyKey;

    @Column(
        name = "request_hash",
        nullable = false,
        length = 64,
        updatable = false
    )
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "failure_code", length = 64)
    private String failureCode;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Transfer() {
        // Constructeur exigé par JPA.
    }

    private Transfer(
        UUID ownerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String requestHash,
        Instant now
    ) {
        this.id = UUID.randomUUID();
        this.ownerId = Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );
        this.sourceAccountId = Objects.requireNonNull(
            sourceAccountId,
            "sourceAccountId is required"
        );
        this.destinationAccountId =
            Objects.requireNonNull(
                destinationAccountId,
                "destinationAccountId is required"
            );

        if (sourceAccountId.equals(
            destinationAccountId
        )) {
            throw new IllegalArgumentException(
                "Source and destination accounts must be different"
            );
        }

        this.amount = normalizeAmount(amount);
        this.currency = normalizeCurrency(currency);
        this.idempotencyKey =
            normalizeIdempotencyKey(idempotencyKey);
        this.requestHash =
            normalizeRequestHash(requestHash);

        this.status = TransferStatus.PENDING;
        this.failureCode = null;
        this.createdAt = requireInstant(now);
        this.updatedAt = now;
        this.completedAt = null;
    }

    public static Transfer start(
        UUID ownerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String requestHash,
        Instant now
    ) {
        return new Transfer(
            ownerId,
            sourceAccountId,
            destinationAccountId,
            amount,
            currency,
            idempotencyKey,
            requestHash,
            now
        );
    }

    public void complete(Instant now) {
        if (status == TransferStatus.COMPLETED) {
            return;
        }

        if (status == TransferStatus.FAILED) {
            throw new IllegalStateException(
                "A failed transfer cannot be completed"
            );
        }

        Instant completionTime =
            requireTerminalTime(now);

        status = TransferStatus.COMPLETED;
        failureCode = null;
        updatedAt = completionTime;
        completedAt = completionTime;
    }

    public void fail(
        String failureCode,
        Instant now
    ) {
        String normalizedCode =
            normalizeFailureCode(failureCode);

        if (status == TransferStatus.FAILED) {
            if (normalizedCode.equals(this.failureCode)) {
                return;
            }

            throw new IllegalStateException(
                "Transfer already failed with another code"
            );
        }

        if (status == TransferStatus.COMPLETED) {
            throw new IllegalStateException(
                "A completed transfer cannot fail"
            );
        }

        Instant failureTime =
            requireTerminalTime(now);

        status = TransferStatus.FAILED;
        this.failureCode = normalizedCode;
        updatedAt = failureTime;
        completedAt = failureTime;
    }

    public boolean isPending() {
        return status == TransferStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == TransferStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransferStatus.FAILED;
    }

    private Instant requireTerminalTime(Instant instant) {
        Instant value = requireInstant(instant);

        if (value.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                "Terminal time cannot precede creation time"
            );
        }

        return value;
    }

    private static BigDecimal normalizeAmount(
        BigDecimal amount
    ) {
        Objects.requireNonNull(
            amount,
            "amount is required"
        );

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                "amount must be positive"
            );
        }

        final BigDecimal normalized;

        try {
            normalized = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                "amount must have at most 2 decimal places",
                exception
            );
        }

        if (normalized.precision() > 19) {
            throw new IllegalArgumentException(
                "amount exceeds the supported precision"
            );
        }

        return normalized;
    }

    private static String normalizeCurrency(
        String currency
    ) {
        String normalized =
            requireText(currency, "currency")
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                "currency must contain exactly 3 letters"
            );
        }

        return normalized;
    }

    private static String normalizeIdempotencyKey(
        String idempotencyKey
    ) {
        String normalized =
            requireText(
                idempotencyKey,
                "idempotencyKey"
            );

        if (normalized.length() < 8
            || normalized.length() > 128) {

            throw new IllegalArgumentException(
                "idempotencyKey length must be between 8 and 128"
            );
        }

        return normalized;
    }

    private static String normalizeRequestHash(
        String requestHash
    ) {
        String normalized =
            requireText(requestHash, "requestHash")
                .toLowerCase(Locale.ROOT);

        if (!normalized.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                "requestHash must be a SHA-256 hexadecimal value"
            );
        }

        return normalized;
    }

    private static String normalizeFailureCode(
        String failureCode
    ) {
        String normalized =
            requireText(failureCode, "failureCode")
                .toUpperCase(Locale.ROOT);

        if (normalized.length() > 64
            || !normalized.matches("[A-Z0-9_]+")) {

            throw new IllegalArgumentException(
                "failureCode has an invalid format"
            );
        }

        return normalized;
    }

    private static String requireText(
        String value,
        String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " is required"
            );
        }

        return value.trim();
    }

    private static Instant requireInstant(
        Instant instant
    ) {
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

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Transfer other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
