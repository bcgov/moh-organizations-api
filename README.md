# MoH Organizations API [![Lifecycle:Stable](https://img.shields.io/badge/Lifecycle-Stable-97ca00)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md) [![Java CI with Maven](https://github.com/sharpedavid/organizations-api/actions/workflows/maven.yml/badge.svg)](https://github.com/sharpedavid/organizations-api/actions/workflows/maven.yml)

A Spring Boot REST API for managing organizations within the Ministry of Health.

All resources require JWT authorization.

## Configuration

The app stores organizations in Postgres, in the table `organizations.organization`. The app never creates or
changes tables: Hibernate only checks at startup that the table matches the entity (`ddl-auto: validate`), and the
app won't start if it doesn't. The scripts that create the role, schema and table are in [db/](db).

Defaults are in [application.yml](src/main/resources/application.yml). On the servers, an external
`application.yaml` in the service's working directory (from SVN, like UMS) supplies the environment's settings,
and secrets come from environment variables. Settings of particular importance:

* `organizations_db_url`, `organizations_db_username`, `organizations_db_password`: the Postgres connection.
* `base-oauth-url`: the token issuer. Defaults to the `KEYCLOAK_AUTH_URL` environment variable.
* `organization-api-client-id`: the client ID of this bearer-only client, used for `aud` validation. The client is
  expected to have `add-org` and `get-org` roles.
* `server.port` and `server.ssl.*`: the port and HTTPS keystore. HTTPS is configured only on the servers.

## Run Locally

### 1. Start Postgres

Any Postgres 17 works. With Podman (on Windows from WSL, use `podman.exe`) or Docker:

```bash
podman run -d --name orgapi-pg -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=kcd \
  docker.io/library/postgres:17

# Role and schema (the DBA's part on the servers), as the postgres superuser
podman exec -i orgapi-pg psql -U postgres -d kcd -v ON_ERROR_STOP=1 \
  -v password=organizations_api < db/create-role-and-schema.sql

# Table, as organizations_api
podman exec -i orgapi-pg psql -U organizations_api -d kcd -v ON_ERROR_STOP=1 \
  < db/create-organization-table.sql
```

JDBC URL for DBeaver or IntelliJ: `jdbc:postgresql://localhost:5432/kcd` (user and password `organizations_api`).

After a reboot, start the container again with `podman start orgapi-pg`.

### 2. Run the app

The `local` profile ([application-local.yml](src/main/resources/application-local.yml)) points at the database
above, uses the DEV Keycloak realm, and listens on plain HTTP port 8082.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or build and run the jar:

```bash
mvn package -DskipTests=true
java -jar target/organizations-api.jar --spring.profiles.active=local
```

To run without Maven installed, use `./mvnw` (or `mvnw.cmd` on Windows) instead of `mvn`.

Check it's up: `GET http://localhost:8082/health` returns `Hello`.

## Running Tests

The tests start the app against the local Postgres from step 1 (see
[application-test.yml](src/test/resources/application-test.yml)) and get a real access token from the DEV Keycloak
realm. Set these environment variables first:

* `ORGANIZATIONS_API_TOKEN_CREDENTIALS`: credentials for the token request. Example values:
  * `client_id=SOME_CLIENT&client_secret=SOME_SECRET&grant_type=client_credentials`
  * `client_id=SOME_CLIENT&username=SOME_USER&password=SOME_PASSWORD&grant_type=password`
* `KEYCLOAK_AUTH_URL`: `https://common-logon-dev.hlth.gov.bc.ca/auth/realms/moh_applications`

Then:

```bash
mvn test
```

In GitHub Actions, the "Java CI with Maven" workflow starts its own Postgres container and runs the `db/` scripts
before building.

During development you may also find the tests written for the IntelliJ HTTP Client useful. They are in
[requests.http](requests.http).

## API Reference

#### Get all organizations

Requires a token with `get-org`.

```http
GET /organizations
Authorization: Bearer {{auth_token}}

RESPONSE: HTTP 200
[
  {
    "organizationId": "00000010",
    "name": "MoH"
  },
  {
    "organizationId": "00002855",
    "name": "Other"
  }
]
```

#### Add an organization

Requires a token with `add-org`.

```http
POST /organizations
Authorization: Bearer {{auth_token}}
Content-Type: application/json

{
  "organizationId": "12345678",
  "name": "Hi Mom"
}

RESPONSE: HTTP 201
Location: http://localhost:8082/organizations/12345678
```

#### Get an organization

Requires a token with `get-org`.

```http
GET /organizations/{{organization-id}}
Authorization: Bearer {{auth_token}}

RESPONSE: HTTP 200
{
  "organizationId": "12345670",
  "name": "Hi Mom"
}
```

#### Update an organization

Requires a token with `add-org`.

The organization ID must exist.

```http
PUT /organizations/{{organization-id}}
Authorization: Bearer {{auth_token}}
Content-Type: application/json

RESPONSE: HTTP 200
{
  "name": "Hi Dad"
}
```

#### Delete an organization

Requires a token with `delete-org`.

The organization ID must exist.

```http
DELETE /organizations/{{organization-id}}
Authorization: Bearer {{auth_token}}

RESPONSE: HTTP 200
```

## Definitions

#### Organization

| Parameter        | Type     | Description            |
| ---------------- | -------- | ---------------------- |
| `organizationId` | `string` | _Unique, Required_     |
| `name`           | `string` | _Optional_             |

Example:

```json
{
  "organizationId": "12345678",
  "name": "Test Name"
}
```
