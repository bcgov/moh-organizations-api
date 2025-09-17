package ca.bc.hlth.mohorganizations;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class DynamoDBConfigProd {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return new AmazonDynamoDBClient()
                .withRegion(Region.getRegion(Regions.CA_CENTRAL_1));
    }
}
