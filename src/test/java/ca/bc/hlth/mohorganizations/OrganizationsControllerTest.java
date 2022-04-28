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
class OrganizationsControllerTest {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    // The credentials used to retrieve an access token.
    // e.g. client_id=PIDP-SERVICE&client_secret=some_secret&scope=email&grant_type=client_credentials
    // e.g. client_id=admin-cli&username=admin&password=admin&grant_type=some_password
    @Value("${ORGANIZATIONS_API_TOKEN_CREDENTIALS}")
    private String credentials;

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

    @DisplayName("GET /organizations without a token should result in HTTP 403 (Unauthorized)")
    @Test
    public void testGetOrganizations_noToken_unauthorized() {
        webClient.get()
                .uri(urlUnderTest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @DisplayName("GET /organizations should return all organizations")
    @Test
    public void testGetOrganizations_withToken_getOrganizations() {

        Map<String, String> org = new HashMap<>();
        org.put("id", "00000010");
        org.put("name", "MoH");
        org.put("resourceId", "resource1");

        getOrgs()
                .expectStatus().isOk()
                .expectBodyList(Map.class).contains(org);
    }

    @DisplayName("POST should create a new organization")
    @Test
    public void testPostOrganizations_newOrganization() {

        Map<String, String> org = new HashMap<>();
        org.put("id", "00000020");
        org.put("name", "Some New Organization");

        addOrg(org)
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().isEmpty();


        getOrgs()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .consumeWith(l -> {
                            List<Map<String, String>> orgs = l.getResponseBody();
                            long count = orgs.stream()
                                    .filter(o -> o.get("id").equals("00000020") && o.get("name").equals("Some New Organization"))
                                    .count();
                            Assertions.assertEquals(1, count);
                        }
                );
    }

    @DisplayName("POST response Location header should match the new organization's resourceId")
    @Test
    public void testPostOrganizations_locationHeaderMatchesResourceId() {

        Map<String, String> org = new HashMap<>();
        org.put("id", "00000020");
        org.put("name", "Some New Organization");

        @SuppressWarnings("ConstantConditions")
        String path = addOrg(org)
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().isEmpty()
                .getResponseHeaders().getLocation().getPath();

        String location = path.substring(path.lastIndexOf('/') + 1);

        Object resourceId = getOrgs()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody()
                .stream()
                .filter(o -> o.get("id").equals("00000020"))
                .findFirst().get()
                .get("resourceId");

        Assertions.assertEquals(resourceId, location);
    }

    @DisplayName("POSTing the same organization twice should result in HTTP 409 (Conflict)")
    @Test
    public void testPostOrganizations_twice_conflict() {

        Map<String, String> org = new HashMap<>();
        org.put("id", "00000020");
        org.put("name", "Some New Organization");

        addOrg(org)
                .expectStatus().isCreated();

        addOrg(org)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

    }

    private WebTestClient.ResponseSpec getOrgs() {
        return webClient.get()
                .uri(urlUnderTest)
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
                .uri(URI.create("https://common-logon-dev.hlth.gov.bc.ca/auth/realms/moh_applications/protocol/openid-connect/token"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(credentials))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyAsJson = (JSONObject) jsonParser.parse(response.body());

        return responseBodyAsJson.get("access_token").toString();
    }


}