package it.gov.pagopa.standintechsupport.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.gov.pagopa.standintechsupport.controller.model.ResponseContainer;
import it.gov.pagopa.standintechsupport.controller.model.StandInStation;
import it.gov.pagopa.standintechsupport.service.SupportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class SupportController {

    @Autowired
    private SupportService supportService;

    @Operation(summary = "Get the list of filtered events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Get the list",
            content = { @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseContainer.class)) }),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content)
    })
    @GetMapping("/events")
    public ResponseContainer getEvents(Optional<String> station, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> from, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> to){
        return supportService.getEvents(station,from.orElse(null),to.orElse(null));
    }

    @Operation(summary = "Get the list of standin station")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get the list",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class)) })
    })
    @GetMapping("/stations")
    public List<StandInStation> getStations(){
        return supportService.getStations();
    }
}
