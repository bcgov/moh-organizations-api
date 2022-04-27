package ca.bc.hlth.mohorganizations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Organization {

    @JsonProperty("id")
    private String organizationId;
    @JsonProperty("name")
    private String organizationName;
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
