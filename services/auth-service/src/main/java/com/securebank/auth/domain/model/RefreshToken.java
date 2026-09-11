package com.securebank.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    private static final Pattern SHA_256_HEX_PATTERN =
        Pattern.compile("^[0-9a-f]{64}$");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        updatable = false
    )
    private User user;

    @Column(
        name = "family_id",
        nullable = false,
        updatable = false
    )
    private UUID familyId;

    @Column(
        name = "token_hash",
        nullable = false,
        unique = true,
        length = 64,
        updatable = false
    )
    private String tokenHash;

    @Column(
        name = "issued_at",
        nullable = false,
        updatable = false
    )
    private Instant issuedAt;

    @Column(
        name = "expires_at",
        nullable = false,
        updatable = false
    )
    private Instant expiresAt;

    @Column(
        name = "family_expires_at",
        nullable = false,
        updatable = false
    )
    private Instant familyExpiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    @Version
    @Column(nullable = false)
    private long version;

    protected RefreshToken() {
        // Constructeur réservé à JPA.
    }

    private RefreshToken(
        User user,
        UUID familyId,
        String tokenHash,
        Instant issuedAt,
        Instant expiresAt,
        Instant familyExpiresAt
    ) {
        this.user = Objects.requireNonNull(
            user,
            "user is required"
        );

        this.familyId = Objects.requireNonNull(
            familyId,
            "familyId is required"
        );

        this.tokenHash = requireSha256Hash(tokenHash);

        this.issuedAt = Objects.requireNonNull(
            issuedAt,
            "issuedAt is required"
        );

        this.expiresAt = Objects.requireNonNull(
            expiresAt,
            "expiresAt is required"
        );

        this.familyExpiresAt = Objects.requireNonNull(
            familyExpiresAt,
            "familyExpiresAt is required"
        );

        validateExpirationDates();
    }

    public static RefreshToken issue(
        User user,
        UUID familyId,
        String tokenHash,
        Instant issuedAt,
        Instant expiresAt,
        Instant familyExpiresAt
    ) {
        return new RefreshToken(
            user,
            familyId,
            tokenHash,
            issuedAt,
            expiresAt,
            familyExpiresAt
        );
    }

    public UUID getId() {
        return id;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    @JsonIgnore
    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getFamilyExpiresAt() {
        return familyExpiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getReplacedByTokenId() {
        return replacedByTokenId;
    }

    public long getVersion() {
        return version;
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(Instant now) {
        Objects.requireNonNull(now, "now is required");

        return !now.isBefore(expiresAt)
            || !now.isBefore(familyExpiresAt);
    }

    public boolean isUsableAt(Instant now) {
        return !isUsed()
            && !isRevoked()
            && !isExpiredAt(now);
    }

    public void consume(
        Instant now,
        UUID replacementTokenId
    ) {
        Objects.requireNonNull(now, "now is required");
        Objects.requireNonNull(
            replacementTokenId,
            "replacementTokenId is required"
        );

        if (isUsed()) {
            throw new IllegalStateException(
                "Refresh token has already been used"
            );
        }

        if (isRevoked()) {
            throw new IllegalStateException(
                "Refresh token has been revoked"
            );
        }

        if (isExpiredAt(now)) {
            throw new IllegalStateException(
                "Refresh token has expired"
            );
        }

        if (id != null && id.equals(replacementTokenId)) {
            throw new IllegalArgumentException(
                "A refresh token cannot replace itself"
            );
        }

        usedAt = now;
        replacedByTokenId = replacementTokenId;
    }

    public void revoke(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (now.isBefore(issuedAt)) {
            throw new IllegalArgumentException(
                "revocation date cannot precede issuance date"
            );
        }

        if (revokedAt == null) {
            revokedAt = now;
        }
    }

    private void validateExpirationDates() {
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                "expiresAt must be after issuedAt"
            );
        }

        if (!familyExpiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                "familyExpiresAt must be after issuedAt"
            );
        }

        if (familyExpiresAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                "familyExpiresAt must not precede expiresAt"
            );
        }
    }

    private static String requireSha256Hash(String tokenHash) {
        if (tokenHash == null
            || !SHA_256_HEX_PATTERN.matcher(tokenHash).matches()) {

            throw new IllegalArgumentException(
                "tokenHash must be a lowercase SHA-256 hexadecimal value"
            );
        }

        return tokenHash;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null
            || Hibernate.getClass(this)
            != Hibernate.getClass(other)) {

            return false;
        }

        RefreshToken otherToken = (RefreshToken) other;

        return id != null && id.equals(otherToken.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
