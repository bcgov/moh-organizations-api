package ca.bc.hlth.mohorganizations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class OrganizationsController {

    OrganizationRepository organizationRepository;

    Logger logger = LoggerFactory.getLogger(OrganizationsController.class);

    public OrganizationsController(OrganizationRepository organizationRepository, AmazonDynamoDB amazonDynamoDB) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping(value = "/organizations/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Organization> getOrganizationById(@PathVariable String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .map((ResponseEntity::ok))
                .orElseGet(() -> ResponseEntity.of(Optional.empty()));
    }

    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Organization> getOrganizations() {
        Collection<Organization> list = new ArrayList<>();
        organizationRepository.findAll().forEach(list::add);
        return list.stream().sorted(Comparator.comparing(org -> Integer.valueOf(org.getOrganizationId()))).collect(Collectors.toList());
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addOrganization(@RequestBody Organization organization) {
        logger.info("CI/CD demo. Add org endpoint was triggered " + organization.getOrganizationName());
        return organizationRepository.findByOrganizationId(organization.getOrganizationId())
                .map(existingOrganization -> ResponseEntity.status(HttpStatus.CONFLICT).build()
                ).orElseGet(() -> {
                    organizationRepository.save(organization);
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{organizationId}")
                            .buildAndExpand(organization.getOrganizationId())
                            .toUri();
                    return ResponseEntity.created(location).build();
                });
    }

    @PutMapping(value = "/organizations/{organizationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> putOrganization(@RequestBody Organization updatedOrganization, @PathVariable String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .map(existingOrganization -> {
                    existingOrganization.setOrganizationName(updatedOrganization.getOrganizationName());
                    organizationRepository.save(existingOrganization);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/organizations/{organizationId}")
    public ResponseEntity<Object> deleteOrganization(@PathVariable String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .map(existingOrganization -> {
                    organizationRepository.deleteById(existingOrganization.getOrganizationId());
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}