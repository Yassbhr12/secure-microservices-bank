package com.securebank.auth.repository;

import com.securebank.auth.domain.model.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
    extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT token
        FROM RefreshToken token
        JOIN FETCH token.user
        WHERE token.tokenHash = :tokenHash
        """)
    Optional<RefreshToken> findByTokenHashForUpdate(
        @Param("tokenHash") String tokenHash
    );

    @Modifying(
        flushAutomatically = true,
        clearAutomatically = true
    )
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = :revokedAt,
            token.version = token.version + 1
        WHERE token.familyId = :familyId
          AND token.revokedAt IS NULL
        """)
    int revokeActiveFamily(
        @Param("familyId") UUID familyId,
        @Param("revokedAt") Instant revokedAt
    );

    @Modifying(
        flushAutomatically = true,
        clearAutomatically = true
    )
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = :revokedAt,
            token.version = token.version + 1
        WHERE token.user.id = :userId
          AND token.revokedAt IS NULL
        """)
    int revokeAllActiveForUser(
        @Param("userId") UUID userId,
        @Param("revokedAt") Instant revokedAt
    );
}
