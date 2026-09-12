CREATE TABLE processed_transfers (
                                     transfer_id UUID PRIMARY KEY,
                                     owner_id UUID NOT NULL,
                                     source_account_id UUID NOT NULL,
                                     destination_account_id UUID NOT NULL,
                                     amount NUMERIC(19, 2) NOT NULL,
                                     currency VARCHAR(3) NOT NULL,
                                     request_hash VARCHAR(64) NOT NULL,
                                     source_balance_after NUMERIC(19, 2) NOT NULL,
                                     destination_balance_after NUMERIC(19, 2) NOT NULL,
                                     processed_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                     CONSTRAINT fk_processed_transfer_source
                                         FOREIGN KEY (source_account_id)
                                             REFERENCES accounts(id),

                                     CONSTRAINT fk_processed_transfer_destination
                                         FOREIGN KEY (destination_account_id)
                                             REFERENCES accounts(id),

                                     CONSTRAINT chk_processed_transfer_accounts
                                         CHECK (source_account_id <> destination_account_id),

                                     CONSTRAINT chk_processed_transfer_amount
                                         CHECK (amount > 0),

                                     CONSTRAINT chk_processed_transfer_currency
                                         CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_processed_transfer_hash
        CHECK (request_hash ~ '^[0-9a-f]{64}$'),

    CONSTRAINT chk_processed_transfer_source_balance
        CHECK (source_balance_after >= 0),

    CONSTRAINT chk_processed_transfer_destination_balance
        CHECK (destination_balance_after >= 0)
);

CREATE INDEX ix_processed_transfers_owner
    ON processed_transfers(owner_id);

CREATE INDEX ix_processed_transfers_source
    ON processed_transfers(source_account_id);

CREATE INDEX ix_processed_transfers_destination
    ON processed_transfers(destination_account_id);
