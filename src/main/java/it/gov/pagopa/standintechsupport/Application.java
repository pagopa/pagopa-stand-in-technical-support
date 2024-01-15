package it.gov.pagopa.standintechsupport; // TODO: refactor the package

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Value("${cosmos.endpoint}")
  private String cosmosEndpoint;

  @Value("${cosmos.key}")
  private String cosmosKey;

  @Bean
  public CosmosClient getCosmosClient() {
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosKey).buildClient();
  }
}
