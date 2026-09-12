CREATE TABLE accounts
(
    id             UUID           NOT NULL,
    owner_id       UUID           NOT NULL,
    account_number VARCHAR(34)    NOT NULL,
    balance        NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'MAD',
    status         VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL,
    version        BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT accounts_pkey
        PRIMARY KEY (id),

    CONSTRAINT ux_accounts_account_number
        UNIQUE (account_number),

    CONSTRAINT chk_accounts_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_accounts_currency
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_accounts_status
        CHECK (status IN ('ACTIVE', 'BLOCKED')),

    CONSTRAINT chk_accounts_timestamps
        CHECK (updated_at >= created_at),

    CONSTRAINT chk_accounts_version
        CHECK (version >= 0)
);

CREATE INDEX ix_accounts_owner_id
    ON accounts (owner_id);

CREATE INDEX ix_accounts_owner_status
    ON accounts (owner_id, status);
