CREATE USER auth_service_user
WITH PASSWORD 'auth_local_password';

CREATE DATABASE auth_db
OWNER auth_service_user;