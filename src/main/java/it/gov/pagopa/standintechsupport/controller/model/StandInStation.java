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
public class StandInStation {
  private String station;
  private Instant timestamp;
}
