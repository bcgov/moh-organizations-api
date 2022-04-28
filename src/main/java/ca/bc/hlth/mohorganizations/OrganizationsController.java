package ca.bc.hlth.mohorganizations;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<Object> addOrganization(@RequestBody Organization organization) {
        if (organizationRepository.findByOrganizationId(organization.getOrganizationId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization with given ID already exists.");
        } else {
            // This is a new organization, so generate a resource identifier.
            organization.setResourceId(UUID.randomUUID().toString());
        }
        organizationRepository.save(organization);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(organization.getResourceId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

}