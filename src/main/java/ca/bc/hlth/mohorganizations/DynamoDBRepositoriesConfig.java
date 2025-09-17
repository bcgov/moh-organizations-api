package ca.bc.hlth.mohorganizations;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDynamoDBRepositories(basePackages = "ca.bc.hlth.mohorganizations")
public class DynamoDBRepositoriesConfig {
    // no beans here, just enables repository scanning
}
