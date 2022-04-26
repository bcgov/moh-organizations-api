package ca.bc.hlth.mohorganizations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class OrganizationsController {

    Logger logger = LoggerFactory.getLogger(OrganizationsController.class);

    private final Set<Map<String, String>> orgs;

    public OrganizationsController() {
        orgs = new HashSet<>();
        Map<String, String> org1 = new HashMap<>();
        org1.put("id", "00000010");
        org1.put("name", "MoH");
        orgs.add(org1);
        Map<String, String> org2 = new HashMap<>();
        org2.put("id", "00002855");
        org2.put("name", "Other");
        orgs.add(org2);
    }

    @GetMapping("/organizations")
    public Collection<?> getOrganizations() {
        return orgs;
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addOrganization(@RequestBody Map<String, String> organization) {
        orgs.add(organization);
    }

}