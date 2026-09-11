package com.securebank.auth.application.security;

import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BankUserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> authorities;

    private BankUserPrincipal(
        UUID userId,
        String email,
        String passwordHash,
        Role role,
        boolean enabled,
        boolean accountNonLocked
    ) {
        this.userId = Objects.requireNonNull(
            userId,
            "userId is required"
        );

        this.email = requireText(
            email,
            "email"
        );

        this.passwordHash = requireText(
            passwordHash,
            "passwordHash"
        );

        this.role = Objects.requireNonNull(
            role,
            "role is required"
        );

        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;

        this.authorities = List.of(
            new SimpleGrantedAuthority(
                "ROLE_" + role.name()
            )
        );
    }

    public static BankUserPrincipal from(
        User user,
        Instant now
    ) {
        Objects.requireNonNull(
            user,
            "user is required"
        );

        Objects.requireNonNull(
            now,
            "now is required"
        );

        return new BankUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getRole(),
            user.isEnabled(),
            !user.isTemporarilyLockedAt(now)
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public String toString() {
        return "BankUserPrincipal["
            + "userId=" + userId
            + ", email=" + email
            + ", role=" + role
            + ", enabled=" + enabled
            + ", accountNonLocked=" + accountNonLocked
            + "]";
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

        return value;
    }
}
