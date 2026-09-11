CREATE TABLE refresh_tokens (

                                id UUID PRIMARY KEY,

                                user_id UUID NOT NULL,

                                family_id UUID NOT NULL,

                                token_hash VARCHAR(64) NOT NULL,

                                issued_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                family_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                used_at TIMESTAMP WITH TIME ZONE,

                                revoked_at TIMESTAMP WITH TIME ZONE,

                                replaced_by_token_id UUID,

                                version BIGINT NOT NULL DEFAULT 0,

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users (id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_refresh_tokens_replacement
                                    FOREIGN KEY (replaced_by_token_id)
                                        REFERENCES refresh_tokens (id)
                                        ON DELETE SET NULL,

                                CONSTRAINT ux_refresh_tokens_token_hash
                                    UNIQUE (token_hash),

                                CONSTRAINT chk_refresh_tokens_hash_length
                                    CHECK (CHAR_LENGTH(token_hash) = 64),

                                CONSTRAINT chk_refresh_tokens_expiration
                                    CHECK (expires_at > issued_at),

                                CONSTRAINT chk_refresh_tokens_family_expiration
                                    CHECK (
                                        family_expires_at >= expires_at
                                            AND family_expires_at > issued_at
                                        ),

                                CONSTRAINT chk_refresh_tokens_used_at
                                    CHECK (
                                        used_at IS NULL
                                            OR used_at >= issued_at
                                        ),

                                CONSTRAINT chk_refresh_tokens_revoked_at
                                    CHECK (
                                        revoked_at IS NULL
                                            OR revoked_at >= issued_at
                                        ),

                                CONSTRAINT chk_refresh_tokens_replacement
                                    CHECK (
                                        replaced_by_token_id IS NULL
                                            OR (
                                            used_at IS NOT NULL
                                                AND replaced_by_token_id <> id
                                            )
                                        ),

                                CONSTRAINT chk_refresh_tokens_version
                                    CHECK (version >= 0)
);

CREATE INDEX ix_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX ix_refresh_tokens_active_family
    ON refresh_tokens (family_id)
    WHERE revoked_at IS NULL;

CREATE INDEX ix_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);
