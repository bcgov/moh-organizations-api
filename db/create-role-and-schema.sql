-- Run by a DBA (or the postgres superuser locally), in the target database.
-- Usage: psql -v ON_ERROR_STOP=1 -v password='<from KeePass>' -f db/create-role-and-schema.sql
CREATE ROLE organizations_api LOGIN PASSWORD :'password';
CREATE SCHEMA organizations AUTHORIZATION organizations_api;
ALTER ROLE organizations_api SET search_path = organizations;
