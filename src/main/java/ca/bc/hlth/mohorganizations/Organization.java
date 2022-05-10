package ca.bc.hlth.mohorganizations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
public class Organization {

    @JsonProperty("organizationId")
    @Column(unique = true)
    private String organizationId;
    @JsonProperty("name")
    private String organizationName;
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false, nullable = false)
    private String resourceId;

    public Organization() {
    }

    public Organization(String id, String name) {
        this.organizationId = id;
        this.organizationName = name;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        if (this.resourceId != null) {
            throw new IllegalStateException("Organization already has a resourceId");
        }
        this.resourceId = resourceId;
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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
