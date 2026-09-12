package com.securebank.account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_transfers")
public class ProcessedTransfer {

    @Id
    @Column(
        name = "transfer_id",
        nullable = false,
        updatable = false
    )
    private UUID transferId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(
        name = "amount",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal amount;

    @Column(
        name = "currency",
        nullable = false,
        length = 3
    )
    private String currency;

    @Column(
        name = "request_hash",
        nullable = false,
        length = 64
    )
    private String requestHash;

    @Column(
        name = "source_balance_after",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal sourceBalanceAfter;

    @Column(
        name = "destination_balance_after",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal destinationBalanceAfter;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedTransfer() {
        // Constructeur JPA.
    }

    private ProcessedTransfer(
        UUID transferId,
        UUID ownerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String requestHash,
        BigDecimal sourceBalanceAfter,
        BigDecimal destinationBalanceAfter,
        Instant processedAt
    ) {
        this.transferId = Objects.requireNonNull(transferId);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.sourceAccountId =
            Objects.requireNonNull(sourceAccountId);
        this.destinationAccountId =
            Objects.requireNonNull(destinationAccountId);
        this.amount = Objects.requireNonNull(amount);
        this.currency = Objects.requireNonNull(currency);
        this.requestHash = Objects.requireNonNull(requestHash);
        this.sourceBalanceAfter =
            Objects.requireNonNull(sourceBalanceAfter);
        this.destinationBalanceAfter =
            Objects.requireNonNull(destinationBalanceAfter);
        this.processedAt =
            Objects.requireNonNull(processedAt);
    }

    public static ProcessedTransfer completed(
        UUID transferId,
        UUID ownerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String requestHash,
        BigDecimal sourceBalanceAfter,
        BigDecimal destinationBalanceAfter,
        Instant processedAt
    ) {
        return new ProcessedTransfer(
            transferId,
            ownerId,
            sourceAccountId,
            destinationAccountId,
            amount,
            currency,
            requestHash,
            sourceBalanceAfter,
            destinationBalanceAfter,
            processedAt
        );
    }

    public boolean represents(
        UUID expectedOwnerId,
        UUID expectedSourceAccountId,
        UUID expectedDestinationAccountId,
        BigDecimal expectedAmount,
        String expectedCurrency,
        String expectedRequestHash
    ) {
        return ownerId.equals(expectedOwnerId)
            && sourceAccountId.equals(expectedSourceAccountId)
            && destinationAccountId.equals(
            expectedDestinationAccountId
        )
            && amount.compareTo(expectedAmount) == 0
            && currency.equals(expectedCurrency)
            && requestHash.equals(expectedRequestHash);
    }

    public UUID getTransferId() {
        return transferId;
    }

    public BigDecimal getSourceBalanceAfter() {
        return sourceBalanceAfter;
    }

    public BigDecimal getDestinationBalanceAfter() {
        return destinationBalanceAfter;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
