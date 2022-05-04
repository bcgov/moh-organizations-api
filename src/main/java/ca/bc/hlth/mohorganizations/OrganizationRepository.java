package ca.bc.hlth.mohorganizations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrganizationId(String organizationId);
    Optional<Organization> findByResourceId(String resourceId);
}
