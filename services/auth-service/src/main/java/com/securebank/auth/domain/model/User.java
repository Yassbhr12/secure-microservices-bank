package com.securebank.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false , nullable = false)
    private UUID id;

    @Column(nullable = false , length = 254)
    private String email;

    @Column(name = "password_hash" , nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    // Constructeur utilisé uniquement par JPA/Hibernate.
    protected User() {
    }

    // Constructeur metier interne contrôlé.

    private User(String email, String passwordHash, Role role) {
        this.email = normalizeEmail(email);
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.role = Objects.requireNonNull(role, "role is required");
        this.enabled = true;
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
    }

    // Factory pour une inscription publique.

    public static User registerClient(String email, String passwordHash) {
        return new User(email, passwordHash, Role.CLIENT);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
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

    // fonction importante

    public void recordLoginFailure(
        int maximumAttempts,
        Duration lockDuration,
        Instant now
    ) {
         if(maximumAttempts <= 0){
             throw new IllegalArgumentException(
                 "maximumAttempts must be greater than zero"
             );
         }

        Objects.requireNonNull(lockDuration, "lockDuration is required");
        Objects.requireNonNull(now, "now is required");

         if(lockDuration.isZero() || lockDuration.isNegative()){
             throw new IllegalArgumentException(
                 "lockDuration must be positive"
             );
         }

         failedLoginAttempts++;

         if (failedLoginAttempts >= maximumAttempts){
             accountLocked = true;
             lockedUntil = now.plus(lockDuration);
         }
    }

    public void recordSuccessfulLogin() {
        failedLoginAttempts = 0;
        accountLocked = false;
        lockedUntil = null;
    }

    public boolean isTemporarilyLockedAt(Instant now) {
        Objects.requireNonNull(now, "now is required");

        return accountLocked
            && lockedUntil != null
            && now.isBefore(lockedUntil);
    }

    public void unlockIfExpired(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (accountLocked
            && lockedUntil != null
            && !now.isBefore(lockedUntil)) {

            accountLocked = false;
            lockedUntil = null;
            failedLoginAttempts = 0;
        }
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    public void changeRole(Role newRole) {
        role = Objects.requireNonNull(newRole, "role is required");
    }

    public void changePasswordHash(String newPasswordHash) {
        passwordHash = requireNonBlank(
            newPasswordHash,
            "newPasswordHash"
        );
    }

    @PrePersist
    private void beforeInsert() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void beforeUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null
            || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }

        User otherUser = (User) other;

        return id != null && id.equals(otherUser.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

    private static String normalizeEmail(String email) {
        String validatedEmail = requireNonBlank(email, "email");

        return validatedEmail
            .trim()
            .toLowerCase(Locale.ROOT);
    }
    private static String requireNonBlank(
        String value,
        String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " is required"
            );
        }

        return value;
    }


}
