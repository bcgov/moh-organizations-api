package ca.bc.hlth.mohorganizations;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
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
// TODO: Look up how this works. Is "test" a default profile? Without it, it didn't load the config file.
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

    private static DynamoDBMapper dynamoDBMapper;

    // https://www.baeldung.com/dynamodb-local-integration-tests
    static {
        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = "8000";
        try {
            DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(
                    new String[]{"-inMemory", "-port", port});
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


        String amazonAWSAccessKey = "access_key";
        String amazonAWSSecretKey = "secret_key";
        String amazonDynamoDBEndpoint = "http://localhost:8000/";

        AmazonDynamoDBClient amazonDynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
        amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Organization.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableRequest);
    }

    @BeforeEach
    public void beforeEach() {
        Organization organization = new Organization("00000010", "MoH");
        dynamoDBMapper.save(organization);
        Organization organization2 = new Organization("00002855", "other");
        dynamoDBMapper.save(organization2);
    }

    @AfterEach
    public void afterEach() {
        PaginatedScanList<Organization> scan = dynamoDBMapper.scan(Organization.class, new DynamoDBScanExpression());
        dynamoDBMapper.batchDelete(scan);
    }


    @BeforeAll
    public void init() throws Exception {
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

        getOrganizations()
                .expectStatus().isOk()
                .expectBodyList(Map.class).contains(org);
    }

    @DisplayName("GET with a known resource ID should return the organization")
    @Test
    public void testGetOrganizations_withResourceId() {
        String knownResourceId = "00000010";
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

        String locationHeaderPath = path.substring(path.lastIndexOf('/') + 1);

        Assertions.assertEquals(org.get("organizationId"), locationHeaderPath);
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
        String myNewId = "my_new_id";
        org.put("organizationId", myNewId);
        org.put("name", "Some New Organization");

        // The controller will ignore the new resource ID.
        putOrg(org, "00002855")
                .expectStatus().isOk();

        getOrganizations()
                .expectBodyList(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .hasSize(2)
                .consumeWith(orgs -> {
                    long count = orgs.getResponseBody().stream().filter(o -> o.get("organizationId").equals(myNewId)).count();
                    Assertions.assertEquals(0, count);
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