CREATE TABLE transfers
(
    id                     UUID           NOT NULL,
    owner_id               UUID           NOT NULL,
    source_account_id      UUID           NOT NULL,
    destination_account_id UUID           NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL DEFAULT 'MAD',
    idempotency_key        VARCHAR(128)   NOT NULL,
    request_hash           VARCHAR(64)    NOT NULL,
    status                 VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    failure_code           VARCHAR(64),
    created_at             TIMESTAMPTZ    NOT NULL,
    updated_at             TIMESTAMPTZ    NOT NULL,
    completed_at           TIMESTAMPTZ,
    version                BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT transfers_pkey
        PRIMARY KEY (id),

    CONSTRAINT ux_transfers_owner_idempotency
        UNIQUE (owner_id, idempotency_key),

    CONSTRAINT chk_transfers_accounts
        CHECK (
            source_account_id
                <> destination_account_id
            ),

    CONSTRAINT chk_transfers_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transfers_currency
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_transfers_idempotency_key
        CHECK (
            char_length(btrim(idempotency_key))
            BETWEEN 8 AND 128
        ),

    CONSTRAINT chk_transfers_request_hash
        CHECK (
            request_hash
            ~ '^[0-9a-f]{64}$'
        ),

    CONSTRAINT chk_transfers_status
        CHECK (
            status IN (
                'PENDING',
                'COMPLETED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_transfers_terminal_state
        CHECK (
            (
                status = 'PENDING'
                AND completed_at IS NULL
            )
            OR
            (
                status IN ('COMPLETED', 'FAILED')
                AND completed_at IS NOT NULL
            )
        ),

    CONSTRAINT chk_transfers_failure
        CHECK (
            (
                status = 'FAILED'
                AND failure_code IS NOT NULL
            )
            OR
            (
                status IN ('PENDING', 'COMPLETED')
                AND failure_code IS NULL
            )
        ),

    CONSTRAINT chk_transfers_timestamps
        CHECK (
            updated_at >= created_at
            AND (
                completed_at IS NULL
                OR completed_at >= created_at
            )
        ),

    CONSTRAINT chk_transfers_version
        CHECK (version >= 0)
);

CREATE INDEX ix_transfers_owner_created_at
    ON transfers (owner_id, created_at DESC);

CREATE INDEX ix_transfers_source_account
    ON transfers (source_account_id);

CREATE INDEX ix_transfers_destination_account
    ON transfers (destination_account_id);

CREATE INDEX ix_transfers_pending_created_at
    ON transfers (created_at)
    WHERE status = 'PENDING';
