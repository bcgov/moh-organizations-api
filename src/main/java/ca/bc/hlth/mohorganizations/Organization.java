package ca.bc.hlth.mohorganizations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization", schema = "organizations")
public class Organization {

    @Id
    @Column(name = "organization_id")
    @JsonProperty("organizationId")
    private String organizationId;

    @Column(name = "name")
    @JsonProperty("name")
    private String organizationName;

    public Organization() {
    }

    public Organization(String id, String name) {
        this.organizationId = id;
        this.organizationName = name;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String id) {
        this.organizationId = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String name) {
        this.organizationName = name;
    }

}
