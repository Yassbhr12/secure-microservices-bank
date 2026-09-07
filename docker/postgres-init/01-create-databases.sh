#!/usr/bin/env bash

set -Eeuo pipefail

: "${AUTH_DB_NAME:?AUTH_DB_NAME is required}"
: "${AUTH_DB_USERNAME:?AUTH_DB_USERNAME is required}"
: "${AUTH_DB_PASSWORD:?AUTH_DB_PASSWORD is required}"

psql \
  --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=auth_db_name="$AUTH_DB_NAME" \
  --set=auth_db_username="$AUTH_DB_USERNAME" \
  --set=auth_db_password="$AUTH_DB_PASSWORD" <<'EOSQL'

CREATE USER :"auth_db_username"
    WITH PASSWORD :'auth_db_password';

CREATE DATABASE :"auth_db_name"
    OWNER :"auth_db_username";

EOSQL