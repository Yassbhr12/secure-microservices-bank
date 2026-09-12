package com.securebank.account.repository;

import com.securebank.account.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
