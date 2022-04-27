package ca.bc.hlth.mohorganizations;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class OrganizationsController {

    OrganizationRepository organizationRepository;

    public OrganizationsController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<?> getOrganizations() {
        return organizationRepository.findAll();
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addOrganization(@RequestBody Organization organization) {
        organizationRepository.save(organization);
    }

}