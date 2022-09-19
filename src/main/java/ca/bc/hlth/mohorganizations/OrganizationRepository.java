package ca.bc.hlth.mohorganizations;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
interface OrganizationRepository extends CrudRepository<Organization, String> {
    Optional<Organization> findByOrganizationId(String organizationId);
}
