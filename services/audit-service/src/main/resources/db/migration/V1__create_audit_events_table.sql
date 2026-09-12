CREATE TABLE audit_events
(
    id              UUID         NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    received_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    source_service  VARCHAR(64)  NOT NULL,
    actor_id        VARCHAR(128),
    actor_role      VARCHAR(32),

    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(64)  NOT NULL,
    resource_id     VARCHAR(128),

    result          VARCHAR(16)  NOT NULL,
    severity        VARCHAR(16)  NOT NULL,

    ip_address      VARCHAR(45),
    correlation_id  VARCHAR(100),
    details         TEXT,

    CONSTRAINT pk_audit_events
        PRIMARY KEY (id),

    CONSTRAINT chk_audit_events_source
        CHECK (BTRIM(source_service) <> ''),

    CONSTRAINT chk_audit_events_action
        CHECK (BTRIM(action) <> ''),

    CONSTRAINT chk_audit_events_resource_type
        CHECK (BTRIM(resource_type) <> ''),

    CONSTRAINT chk_audit_events_result
        CHECK (result IN ('SUCCESS', 'FAILURE')),

    CONSTRAINT chk_audit_events_severity
        CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

CREATE INDEX ix_audit_events_occurred_at
    ON audit_events (occurred_at DESC);

CREATE INDEX ix_audit_events_actor_id
    ON audit_events (actor_id);

CREATE INDEX ix_audit_events_action
    ON audit_events (action);

CREATE INDEX ix_audit_events_severity
    ON audit_events (severity);

CREATE INDEX ix_audit_events_source_service
    ON audit_events (source_service);

CREATE INDEX ix_audit_events_correlation_id
    ON audit_events (correlation_id);

CREATE OR REPLACE FUNCTION prevent_audit_event_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
    RAISE EXCEPTION
        'Audit events are immutable and cannot be updated or deleted';
END;
$$;

CREATE TRIGGER trg_prevent_audit_event_mutation
    BEFORE UPDATE OR DELETE
ON audit_events
    FOR EACH ROW
EXECUTE FUNCTION prevent_audit_event_mutation();
