package it.gov.pagopa.standintechsupport.repository;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import it.gov.pagopa.standintechsupport.repository.model.CosmosEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import it.gov.pagopa.standintechsupport.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CosmosEventsRepository {

  @Autowired private CosmosClient cosmosClient;

  @Value("${cosmos.db.name}")
  private String dbname;

  public static String tablename = "events";

  private CosmosPagedIterable<CosmosEvent> query(SqlQuerySpec query) {
    log.info("executing query:" + query.getQueryText());
    CosmosContainer container = cosmosClient.getDatabase(dbname).getContainer(tablename);
    return container.queryItems(query, new CosmosQueryRequestOptions(), CosmosEvent.class);
  }

  public void newEvent(String type, String info) {
    save(
        CosmosEvent.builder()
            .id(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .info(info)
            .type(type)
            .build());
  }

  public CosmosItemResponse<CosmosEvent> save(CosmosEvent item) {
    CosmosContainer container = cosmosClient.getDatabase(dbname).getContainer(tablename);
    return container.createItem(item);
  }

  public List<CosmosEvent> find(
          Optional<String> station,
          LocalDate dateFrom,
          LocalDate dateTo) {
    List<SqlParameter> paramList;
    if(station.isEmpty()){
      paramList = Arrays.asList(
              new SqlParameter("@from", Util.format(dateFrom)),
              new SqlParameter("@to", Util.format(dateTo.plusDays(1)))
      );
    }else{
      paramList = Arrays.asList(
              new SqlParameter("@from", Util.format(dateFrom)),
              new SqlParameter("@to", Util.format(dateTo.plusDays(1))),
              new SqlParameter("@station",station.get())
      );
    }

    SqlQuerySpec q =
            new SqlQuerySpec(
                    "SELECT * FROM c where c.PartitionKey >= @from and c.PartitionKey < @to"
                            + station.map(pt->" and c.station = @station").orElse("")
                            + " order by c.timestamp desc"
            )
                    .setParameters(paramList);
    String continuationToken = null;
    List<CosmosEvent> results  = new ArrayList<>();
    do{
      Iterable<FeedResponse<CosmosEvent>> feedResponses = query(q).iterableByPage(continuationToken,100);
      for (FeedResponse<CosmosEvent> page : feedResponses) {
        results.addAll(page.getResults());
        continuationToken = page.getContinuationToken();
      }
    }while (continuationToken!=null);

    return results;
  }
}
