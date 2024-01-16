package it.gov.pagopa.standintechsupport;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.standintechsupport.controller.model.ResponseContainer;
import it.gov.pagopa.standintechsupport.controller.model.StandInStation;
import it.gov.pagopa.standintechsupport.repository.model.CosmosEvent;
import it.gov.pagopa.standintechsupport.repository.model.CosmosStandInStation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.CosmosDBEmulatorContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {Initializer.class})
class ApiTest {

  @Autowired ObjectMapper objectMapper;

  @Autowired private MockMvc mvc;

    private static final CosmosDBEmulatorContainer emulator = Initializer.getEmulator();

  @Test
  void swaggerSpringPlugin() throws Exception {

      CosmosClient client = new CosmosClientBuilder().directMode().endpointDiscoveryEnabled(false)
              .endpoint(emulator.getEmulatorEndpoint()).key(emulator.getEmulatorKey()).buildClient();
      client.createDatabaseIfNotExists("standin");
      client.getDatabase("standin").createContainer("events","/PartitionKey");
      client.getDatabase("standin").createContainer("stand_in_stations","/PartitionKey");

      client.getDatabase("standin").getContainer("events").createItem(
              CosmosEvent.builder().id(UUID.randomUUID().toString()).type("TEST").info("test").station("test").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("events").createItem(
              CosmosEvent.builder().id(UUID.randomUUID().toString()).type("TEST").info("test").station("test2").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("stand_in_stations").createItem(
              CosmosStandInStation.builder().id(UUID.randomUUID().toString()).station("test1").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("stand_in_stations").createItem(
              CosmosStandInStation.builder().id(UUID.randomUUID().toString()).station("test2").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("stand_in_stations").createItem(
              CosmosStandInStation.builder().id(UUID.randomUUID().toString()).station("test3").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("stand_in_stations").createItem(
              CosmosStandInStation.builder().id(UUID.randomUUID().toString()).station("test4").timestamp(Instant.now()).build()
      );
      client.getDatabase("standin").getContainer("stand_in_stations").createItem(
              CosmosStandInStation.builder().id(UUID.randomUUID().toString()).station("test5").timestamp(Instant.now()).build()
      );


      mvc.perform(MockMvcRequestBuilders.get("/events").accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
              .andDo(
                      (result) -> {
                          assertNotNull(result);
                          assertNotNull(result.getResponse());
                          final String content = result.getResponse().getContentAsString();
                          assertFalse(content.isBlank());
                          ResponseContainer res =
                                  objectMapper.readValue(result.getResponse().getContentAsString(), ResponseContainer.class);
                          assertEquals(res.getData().size(),2);
                      });

      mvc.perform(MockMvcRequestBuilders.get("/events").queryParam("station","test").accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        .andDo(
            (result) -> {
              assertNotNull(result);
              assertNotNull(result.getResponse());
              final String content = result.getResponse().getContentAsString();
              assertFalse(content.isBlank());
                ResponseContainer res =
                        objectMapper.readValue(result.getResponse().getContentAsString(), ResponseContainer.class);
                assertEquals(res.getData().size(),1);
            });

      mvc.perform(MockMvcRequestBuilders.get("/stations").accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
              .andDo(
                      (result) -> {
                          assertNotNull(result);
                          assertNotNull(result.getResponse());
                          final String content = result.getResponse().getContentAsString();
                          assertFalse(content.isBlank());
                          List<StandInStation> res =
                                  objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
                          assertEquals(res.size(),5);
                      });
  }
}
