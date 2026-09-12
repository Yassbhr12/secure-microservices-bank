#!/usr/bin/env bash

set -Eeuo pipefail

: "${AUDIT_DB_NAME:?AUDIT_DB_NAME is required}"
: "${AUDIT_DB_USERNAME:?AUDIT_DB_USERNAME is required}"
: "${AUDIT_DB_PASSWORD:?AUDIT_DB_PASSWORD is required}"

psql \
  --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=service_db_name="$AUDIT_DB_NAME" \
  --set=service_db_username="$AUDIT_DB_USERNAME" \
  --set=service_db_password="$AUDIT_DB_PASSWORD" <<'EOSQL'

SELECT format(
    'CREATE ROLE %I LOGIN PASSWORD %L',
    :'service_db_username',
    :'service_db_password'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_catalog.pg_roles
    WHERE rolname = :'service_db_username'
)
\gexec

ALTER ROLE :"service_db_username"
    WITH LOGIN PASSWORD :'service_db_password';

SELECT format(
    'CREATE DATABASE %I OWNER %I',
    :'service_db_name',
    :'service_db_username'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = :'service_db_name'
)
\gexec

EOSQL
