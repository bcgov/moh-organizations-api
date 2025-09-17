package ca.bc.hlth.mohorganizations;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class DynamoDBConfigLocal {

    @Bean(destroyMethod = "stop")
    public DynamoDBProxyServer dynamoDBProxyServer() throws Exception {
        System.setProperty("sqlite4java.library.path", "native-libs");

        DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", "8000"});
        server.start();

        // create the Organization table for dev convenience
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(
                new BasicAWSCredentials("access_key", "secret_key"));
        client.setEndpoint("http://localhost:8000/");

        DynamoDBMapper mapper = new DynamoDBMapper(client);
        CreateTableRequest tableRequest = mapper.generateCreateTableRequest(Organization.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        client.createTable(tableRequest);

        return server;
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(
                new BasicAWSCredentials("access_key", "secret_key"));
        client.setEndpoint("http://localhost:8000/");
        return client;
    }
}
