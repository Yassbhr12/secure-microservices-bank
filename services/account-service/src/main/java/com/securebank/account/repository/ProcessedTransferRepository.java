package com.securebank.account.repository;

import com.securebank.account.domain.model.ProcessedTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedTransferRepository
    extends JpaRepository<ProcessedTransfer, UUID> {
}
