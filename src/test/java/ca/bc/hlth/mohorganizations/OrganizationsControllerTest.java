package ca.bc.hlth.mohorganizations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.NamingException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrganizationsControllerTest {

    @LocalServerPort
    private int port;

    private String urlUnderTest;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void init() {
        urlUnderTest = "http://localhost:" + port + "/organizations";
//        urlUnderTest = "https://common-logon-dev.hlth.gov.bc.ca/ldap/users";
    }

    @Test
    public void testGetOrganizations_noToken_unauthorized() {
        ResponseEntity<Object> response = restTemplate.getForEntity(urlUnderTest, Object.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

}