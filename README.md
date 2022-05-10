[![Java CI with Maven](https://github.com/sharpedavid/organizations-api/actions/workflows/maven.yml/badge.svg)](https://github.com/sharpedavid/organizations-api/actions/workflows/maven.yml)

# Organizations API

A Spring Boot REST API for managing organizations within the Ministry of Health.

All resources require JWT authorization.

## Configuration

Application configuration is specified in [application.yml](src/main/resources/application.yml). Of particular importance are:

* `base-oauth-url`: the token issuer.
* `organization-api-client-id`: the client ID of this bearer-only client, used for `aud` validation. The client is
  expected to have `add-org` and `get-org` roles.

## Environment variables

The application does not use environment variables, but to run the integration tests you will need to provide
credentials for the token issuer.

`ORGANIZATIONS_API_TOKEN_CREDENTIALS`

Example values:

* `client_id=SOME_CLIENT&client_secret=SOME_SECRET&grant_type=client_credentials`
* `client_id=SOME_CLIENT&username=SOME_USER&password=SOME_PASSWORD&grant_type=password`

## Run Locally

To run with Maven:

```bash
mvn spring-boot:run
```

To run without Maven installed:

```bash
./mvnw spring-boot:run
# or double-click mvnw.cmd on Windows
```

To compile and then run with Java:

```bash
mvn package -DskipTests=true
java -jar target/organizations-api.jar
```

## Running Tests

To run just the tests:

```bash
mvn test
```

During development you may also find the tests written for the IntelliJ HTTP Client useful. They are in [requests.http](requests.http).

## API Reference

#### Get all organizations

Requires a token with `get-org`.

```http
GET /organizations
Authorization: Bearer {{auth_token}}

RESPONSE: HTTP 200
[
  {
    "resourceId": "resource1",
    "id": "00000010",
    "name": "MoH"
  },
  {
    "resourceId": "resource2",
    "id": "00002855",
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
  "id": "12345678",
  "name": "Hi Mom"
}

RESPONSE: HTTP 200
Location: http://localhost:8082/organizations/fd5186d7-649e-486d-acce-38b5fd30c1dd
```

#### Get an organization

Requires a token with `get-org`.

```http
GET /organizations/{{resource-id}}
Authorization: Bearer {{auth_token}}

RESPONSE: HTTP 200
{
  "resourceId": "fd5186d7-649e-486d-acce-38b5fd30c1dd",
  "id": "12345670",
  "name": "Hi Mom"
}
```

#### Update an organization

Requires a token with `add-org`.

The resource ID must exist.

```http
PUT {{api}}/{{resource-id}}
Authorization: Bearer {{auth_token}}
Content-Type: application/json

RESPONSE: HTTP 200
{
  "id": "12345678",
  "name": "Hi Dad"
}
```

## Definitions

#### Organization

| Parameter    | Type     | Description              |
| ------------ | -------- | ------------------------ |
| `id`         | `string` | _Unique, Required_       |
| `name`       | `string` | _Optional_               |
| `resourceId` | `string` | _Generated, Immutable_   |

Example:

```json
{
  "resourceId": "f69de397-d3eb-4db6-be26-083ca89c05e7",
  "id": "12345678",
  "name": "Test Name"
}
```