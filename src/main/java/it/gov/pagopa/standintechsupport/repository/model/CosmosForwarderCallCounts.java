package it.gov.pagopa.standintechsupport.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmosForwarderCallCounts {
  private String id;
  private String station;
  private Instant timestamp;
  private Boolean outcome;

  @JsonProperty("PartitionKey")
  public String getPartitionKey() {
    return timestamp.toString().substring(0, 10);
  }
}
