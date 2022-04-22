package ca.bc.hlth.mohorganizations;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrganizationsController {

    @GetMapping("/organizations")
    public List<Object> organizations() {
        Map<String, String> org1 = new HashMap<>();
        org1.put("id", "00000010");
        org1.put("name", "MoH");
        Map<String, String> org2 = new HashMap<>();
        org2.put("id", "00002855");
        org2.put("name", "Other");
        return Arrays.asList(org1, org2);
    }
}