package it.gov.pagopa.standintechsupport; // TODO: refactor the package

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@SpringBootApplication()
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Value("${cosmos.endpoint}")
  private String cosmosEndpoint;

  @Value("${cosmos.key}")
  private String cosmosKey;

  @Value("${aws.region}")
  private String region;

  @Bean
  public CosmosClient getCosmosClient() {
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosKey).buildClient();
  }

  @Bean
  public SesClient sesClient() {
    return SesClient.builder()
            .region(Region.of(region))
            .build();
  }

}
