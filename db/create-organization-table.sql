-- Run as organizations_api, after db/create-role-and-schema.sql.
-- The app does not create or change tables.
-- organization_id is text, not a number, so leading zeros are kept (e.g. 00000010).
CREATE TABLE organizations.organization (
    organization_id VARCHAR(64)  PRIMARY KEY,
    name            VARCHAR(255)
);
