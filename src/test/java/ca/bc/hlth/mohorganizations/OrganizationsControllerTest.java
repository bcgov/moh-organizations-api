package ca.bc.hlth.mohorganizations;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(value = {"/loadTestData.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class OrganizationsControllerTest {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    // The credentials used to retrieve an access token.
    // e.g. client_id=PIDP-SERVICE&client_secret=some_secret&scope=email&grant_type=client_credentials
    // e.g. client_id=admin-cli&username=admin&password=admin&grant_type=some_password
    @Value("${ORGANIZATIONS_API_TOKEN_CREDENTIALS}")
    private String credentials;
    @Value("${ORGANIZATIONS_API_TOKEN_URL}")
    private String tokenUrl;

    @LocalServerPort
    private int port;

    private String urlUnderTest;

    @Autowired
    private WebTestClient webClient;

    private String accessToken;

    @BeforeAll
    public void init() throws IOException, ParseException, InterruptedException {
        accessToken = getKcAccessToken();
        urlUnderTest = "http://localhost:" + port + "/organizations";
//        urlUnderTest = "https://common-logon-dev.hlth.gov.bc.ca/ldap/users";
    }

    @DisplayName("GET without a token should result in HTTP 403 (Unauthorized)")
    @Test
    public void testGetOrganizations_noToken_unauthorized() {
        webClient.get()
                .uri(urlUnderTest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @DisplayName("GET should return all organizations")
    @Test
    public void testGetOrganizations_withToken_getOrganizations() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000010");
        org.put("name", "MoH");
        org.put("resourceId", "resource1");

        getOrganizations()
                .expectStatus().isOk()
                .expectBodyList(Map.class).contains(org);
    }

    @DisplayName("GET with a known resource ID should return the organization")
    @Test
    public void testGetOrganizations_withResourceId() {
        String knownResourceId = "resource1";
        getOrganization(knownResourceId)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .consumeWith(o -> {
                            String actualId = o.getResponseBody().get("organizationId");
                            Assertions.assertEquals("00000010", actualId);
                        }
                );
    }

    @DisplayName("POST should create a new organization")
    @Test
    public void testPostOrganizations_newOrganization() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000020");
        org.put("name", "Some New Organization");

        addOrg(org)
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().isEmpty();


        getOrganizations()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .consumeWith(l -> {
                            List<Map<String, String>> orgs = l.getResponseBody();
                            long count = orgs.stream()
                                    .filter(o -> o.get("organizationId").equals("00000020") && o.get("name").equals("Some New Organization"))
                                    .count();
                            Assertions.assertEquals(1, count);
                        }
                );
    }

    @DisplayName("POST response Location header should match the new organization's resourceId")
    @Test
    public void testPostOrganizations_locationHeaderMatchesResourceId() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000020");
        org.put("name", "Some New Organization");

        @SuppressWarnings("ConstantConditions")
        String path = addOrg(org)
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().isEmpty()
                .getResponseHeaders().getLocation().getPath();

        String location = path.substring(path.lastIndexOf('/') + 1);

        Object resourceId = getOrganizations()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody()
                .stream()
                .filter(o -> o.get("organizationId").equals("00000020"))
                .findFirst().get()
                .get("resourceId");

        Assertions.assertEquals(resourceId, location);
    }

    @DisplayName("POSTing the same organization twice should result in HTTP 409 (Conflict)")
    @Test
    public void testPostOrganizations_twice_conflict() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000020");
        org.put("name", "Some New Organization");

        addOrg(org)
                .expectStatus().isCreated();

        addOrg(org)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

    }

    @DisplayName("PUT should update an existing organization")
    @Test
    public void testPutOrganizations_updateOrg() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000020");
        org.put("name", "Some New Organization");

        String path = addOrg(org)
                .expectStatus().isCreated()
                .expectBody().isEmpty()
                .getResponseHeaders()
                .getLocation().getPath();

        String resourceId = path.substring(path.lastIndexOf('/') + 1);

        String expectedName = "A Brand New Name";
        org.put("name", expectedName);

        putOrg(org, resourceId)
                .expectStatus().isOk();

        Object actualName = getOrganizations()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody()
                .stream()
                .filter(o -> o.get("organizationId").equals("00000020"))
                .findFirst().get()
                .get("name");

        Assertions.assertEquals(expectedName, actualName);
    }

    @DisplayName("PUT should not update the resource ID")
    @Test
    public void testPutOrganizations_updateResourceId_ignore() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000020");
        org.put("name", "Some New Organization");
        org.put("resourceId", "newResourceId");

        // The controller will ignore the new resource ID.
        putOrg(org, "resource1")
                .expectStatus().isOk();

        getOrganizations()
                .expectBodyList(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .hasSize(2)
                .consumeWith(orgs -> {
                    long count = orgs.getResponseBody().stream().filter(o -> !o.get("resourceId").equals("newResourceId")).count();
                    Assertions.assertEquals(2, count);
                });
    }

    @DisplayName("PUT should return a 404 if the organization does not exist")
    @Test
    public void testPutOrganization_doesNotExist_404() {

        Map<String, String> org = new HashMap<>();
        org.put("organizationId", "00000010");
        org.put("name", "Some New Organization");

        putOrg(org, "some-resource-id-that-does-not-exist")
                .expectStatus().isNotFound();
    }

    private WebTestClient.ResponseSpec putOrg(Map<String, String> org, String location) {
        return webClient.put()
                .uri(urlUnderTest + "/{resource-id}", location)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(org)
                .exchange();
    }

    private WebTestClient.ResponseSpec getOrganizations() {
        return webClient.get()
                .uri(urlUnderTest)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();
    }

    private WebTestClient.ResponseSpec getOrganization(String knownResourceId) {
        return webClient.get()
                .uri(urlUnderTest + "/{resource-id}", knownResourceId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();
    }

    private WebTestClient.ResponseSpec addOrg(Map<String, String> org) {
        return webClient.post()
                .uri(urlUnderTest)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(org)
                .exchange();
    }

    private String getKcAccessToken() throws IOException, InterruptedException, ParseException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                // TODO: The URL should not be hardcoded, or at least it should not be buried down here.
                .uri(URI.create(tokenUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(credentials))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyAsJson = (JSONObject) jsonParser.parse(response.body());

        return responseBodyAsJson.get("access_token").toString();
    }


}