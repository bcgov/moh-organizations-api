package ca.bc.hlth.mohorganizations;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.dynamodb.services.local.main.ServerRunner;
import software.amazon.dynamodb.services.local.server.DynamoDBProxyServer;

@Configuration
@Profile("local")
public class DynamoDBConfigLocal {

    @Bean(destroyMethod = "stop")
    public DynamoDBProxyServer dynamoDBProxyServer() throws Exception {
        DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", "8000"});
        server.start();

        AmazonDynamoDB client = buildLocalClient();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        CreateTableRequest tableRequest = mapper.generateCreateTableRequest(Organization.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        client.createTable(tableRequest);

        return server;
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return buildLocalClient();
    }

    private AmazonDynamoDB buildLocalClient() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("anykey", "anysecret")))
                .build();
    }
}
