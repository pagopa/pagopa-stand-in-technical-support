package it.gov.pagopa.standintechsupport.controller.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmosEventModel {
  private String id;
  private String station;
  private Instant timestamp;
  private String type;
  private String info;

  public String getDate() {
    return timestamp.toString();
  }
}
