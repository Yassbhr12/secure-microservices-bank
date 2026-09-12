#!/usr/bin/env bash

set -Eeuo pipefail

: "${ACCOUNT_DB_NAME:?ACCOUNT_DB_NAME is required}"
: "${ACCOUNT_DB_USERNAME:?ACCOUNT_DB_USERNAME is required}"
: "${ACCOUNT_DB_PASSWORD:?ACCOUNT_DB_PASSWORD is required}"

psql \
  --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=account_db_name="$ACCOUNT_DB_NAME" \
  --set=account_db_username="$ACCOUNT_DB_USERNAME" \
  --set=account_db_password="$ACCOUNT_DB_PASSWORD" <<'EOSQL'

SELECT format(
    'CREATE ROLE %I LOGIN PASSWORD %L',
    :'account_db_username',
    :'account_db_password'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_catalog.pg_roles
    WHERE rolname = :'account_db_username'
)
\gexec

ALTER ROLE :"account_db_username"
    WITH LOGIN PASSWORD :'account_db_password';

SELECT format(
    'CREATE DATABASE %I OWNER %I',
    :'account_db_name',
    :'account_db_username'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = :'account_db_name'
)
\gexec

EOSQL
