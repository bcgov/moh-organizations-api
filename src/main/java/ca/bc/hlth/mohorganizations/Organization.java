package ca.bc.hlth.mohorganizations;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonProperty;

@DynamoDBTable(tableName = "Organization")
public class Organization {

    @DynamoDBHashKey(attributeName = "id")
    @JsonProperty("organizationId")
    private String organizationId;
    @JsonProperty("name")
    private String organizationName;

    public Organization() {
    }

    public Organization(String id, String name) {
        this.organizationId = id;
        this.organizationName = name;
    }

    @DynamoDBAttribute
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String id) {
        this.organizationId = id;
    }

    @DynamoDBAttribute
    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String name) {
        this.organizationName = name;
    }

}
