package com.securebank.transaction.repository;

import com.securebank.transaction.domain.model.Transfer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository
    extends JpaRepository<Transfer, UUID> {

    Optional<Transfer>
    findByOwnerIdAndIdempotencyKey(
        UUID ownerId,
        String idempotencyKey
    );

    Optional<Transfer> findByIdAndOwnerId(
        UUID transferId,
        UUID ownerId
    );

    List<Transfer>
    findAllByOwnerIdOrderByCreatedAtDesc(
        UUID ownerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT transfer
        FROM Transfer transfer
        WHERE transfer.ownerId = :ownerId
          AND transfer.idempotencyKey = :idempotencyKey
        """)
    Optional<Transfer>
    findByOwnerAndIdempotencyKeyForUpdate(
        @Param("ownerId") UUID ownerId,
        @Param("idempotencyKey")
        String idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT transfer
    FROM Transfer transfer
    WHERE transfer.id = :transferId
    """)
    Optional<Transfer> findByIdForUpdate(
        @Param("transferId")
        UUID transferId
    );
}
