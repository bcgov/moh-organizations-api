package ca.bc.hlth.mohorganizations;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = "/organizations/{resourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Organization> getOrganizationById(@PathVariable String resourceId) {
        return organizationRepository.findByResourceId(resourceId)
                .map((ResponseEntity::ok))
                .orElseGet(() -> ResponseEntity.of(Optional.empty()));
    }

    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Organization> getOrganizations() {
        return organizationRepository.findAll();
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addOrganization(@RequestBody Organization organization) {
        return organizationRepository.findByOrganizationId(organization.getOrganizationId())
                .map(existingOrganization -> ResponseEntity.status(HttpStatus.CONFLICT).build()
                ).orElseGet(() -> {
                    // This is a new organization, so generate a resource identifier.
                    organization.setResourceId(UUID.randomUUID().toString());
                    organizationRepository.save(organization);
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{resourceId}")
                            .buildAndExpand(organization.getResourceId())
                            .toUri();
                    return ResponseEntity.created(location).build();
                });
    }

    @PutMapping(value = "/organizations/{resourceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> putOrganization(@RequestBody Organization updatedOrganization, @PathVariable String resourceId) {
        return organizationRepository.findByResourceId(resourceId)
                .map(existingOrganization -> {
                    existingOrganization.setOrganizationId(updatedOrganization.getOrganizationId());
                    existingOrganization.setOrganizationName(updatedOrganization.getOrganizationName());
                    organizationRepository.save(existingOrganization);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}