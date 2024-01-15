package it.gov.pagopa.standintechsupport.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ResponseContainer {
  private LocalDate dateFrom;
  private LocalDate dateTo;

  private int count;

  @JsonProperty("data")
  private List<CosmosEventModel> data;
}
