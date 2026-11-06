package ca.bc.hlth.mohorganizations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface OrganizationRepository extends JpaRepository<Organization, String> {
    Optional<Organization> findByOrganizationId(String organizationId);
}
