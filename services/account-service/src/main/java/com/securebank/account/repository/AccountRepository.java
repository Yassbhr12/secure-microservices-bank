package com.securebank.account.repository;

import com.securebank.account.domain.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
    extends JpaRepository<Account, UUID> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(
        String accountNumber
    );

    Optional<Account> findByIdAndOwnerId(
        UUID accountId,
        UUID ownerId
    );

    List<Account> findAllByOwnerIdOrderByCreatedAtDesc(
        UUID ownerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT account
        FROM Account account
        WHERE account.id IN :accountIds
        ORDER BY account.id
        """)
    List<Account> findAllByIdForUpdate(
        @Param("accountIds")
        List<UUID> accountIds
    );
}
