package ca.bc.hlth.mohorganizations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
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
import java.util.Optional;

@RestController
public class OrganizationsController {

    OrganizationRepository organizationRepository;

    Logger logger = LoggerFactory.getLogger(OrganizationsController.class);

    public OrganizationsController(OrganizationRepository organizationRepository, AmazonDynamoDB amazonDynamoDB) {
        this.organizationRepository = organizationRepository;

        DynamoDBMapper  dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        try {
            CreateTableRequest tableRequest = dynamoDBMapper
                    .generateCreateTableRequest(Organization.class);
            tableRequest.setProvisionedThroughput(
                    new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
            // TODO: Figure out how we should actually handle this.
            logger.info("Exception expected if table already exists.", e);
        }

    }

    @GetMapping(value = "/organizations/{resourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Organization> getOrganizationById(@PathVariable String resourceId) {
        return organizationRepository.findByOrganizationId(resourceId)
                .map((ResponseEntity::ok))
                .orElseGet(() -> ResponseEntity.of(Optional.empty()));
    }

    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Organization> getOrganizations() {
        Collection<Organization> list = new ArrayList<>();
        organizationRepository.findAll().forEach(list::add);
        return list;
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addOrganization(@RequestBody Organization organization) {
        return organizationRepository.findByOrganizationId(organization.getOrganizationId())
                .map(existingOrganization -> ResponseEntity.status(HttpStatus.CONFLICT).build()
                ).orElseGet(() -> {
                    organizationRepository.save(organization);
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{resourceId}")
                            .buildAndExpand(organization.getOrganizationId())
                            .toUri();
                    return ResponseEntity.created(location).build();
                });
    }

    @PutMapping(value = "/organizations/{resourceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> putOrganization(@RequestBody Organization updatedOrganization, @PathVariable String resourceId) {
        return organizationRepository.findByOrganizationId(resourceId)
                .map(existingOrganization -> {
                    existingOrganization.setOrganizationId(updatedOrganization.getOrganizationId());
                    existingOrganization.setOrganizationName(updatedOrganization.getOrganizationName());
                    organizationRepository.save(existingOrganization);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}