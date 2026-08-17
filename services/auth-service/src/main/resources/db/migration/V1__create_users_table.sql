CREATE TABLE users (

                       id UUID PRIMARY KEY,

                       email VARCHAR(254) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       role VARCHAR(40) NOT NULL,

                       enabled BOOLEAN NOT NULL DEFAULT TRUE,

                       account_locked BOOLEAN NOT NULL DEFAULT FALSE,

                       failed_login_attempts INTEGER NOT NULL DEFAULT 0,

                       locked_until TIMESTAMP WITH TIME ZONE,

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       version BIGINT NOT NULL DEFAULT 0,

                       CONSTRAINT chk_users_role
                           CHECK (
                               role IN (
                                        'CLIENT',
                                        'ADMIN',
                                        'AUDITOR',
                                        'SECURITY_VIEWER'
                                   )
                               ),

                       CONSTRAINT chk_failed_login_attempts
                           CHECK (failed_login_attempts >= 0)
);

CREATE UNIQUE INDEX ux_users_email
    ON users (LOWER(email));