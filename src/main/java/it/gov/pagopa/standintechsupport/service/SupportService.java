package it.gov.pagopa.standintechsupport.service;

import it.gov.pagopa.standintechsupport.controller.model.CosmosEventModel;
import it.gov.pagopa.standintechsupport.controller.model.ResponseContainer;
import it.gov.pagopa.standintechsupport.controller.model.StandInStation;
import it.gov.pagopa.standintechsupport.exception.AppError;
import it.gov.pagopa.standintechsupport.exception.AppException;
import it.gov.pagopa.standintechsupport.model.DateRequest;
import it.gov.pagopa.standintechsupport.repository.BlacklistStationsRepository;
import it.gov.pagopa.standintechsupport.repository.CosmosEventsRepository;
import it.gov.pagopa.standintechsupport.repository.CosmosStationRepository;
import it.gov.pagopa.standintechsupport.repository.StandInStationsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SupportService {

    @Value("#{T(java.lang.Integer).parseInt('${date-range-limit}')}")
    Integer dateRangeLimit;

    @Autowired
    private BlacklistStationsRepository blacklistStationsRepository;
    @Autowired
    private CosmosStationRepository standInStationsRepository;
    @Autowired
    private CosmosEventsRepository cosmosEventsRepository;

    public ResponseContainer getEvents(Optional<String> station, LocalDate from, LocalDate to) {
        DateRequest dateRequest = verifyDate(from, to);
        List<CosmosEventModel> collect = cosmosEventsRepository.find(station, dateRequest.getFrom(), dateRequest.getTo()).stream().map(ee -> {
            return CosmosEventModel.builder()
                    .id(ee.getId())
                    .type(ee.getType())
                    .info(ee.getInfo())
                    .timestamp(ee.getTimestamp())
                    .station(ee.getStation()).build();
        }).collect(Collectors.toList());
        return ResponseContainer.builder().count(collect.size())
                .data(collect)
                .dateFrom(dateRequest.getFrom())
                .dateTo(dateRequest.getTo()).build();
    }

    public List<StandInStation> getStations() {
      return standInStationsRepository.getStations().stream().map(s->StandInStation.builder().station(s.getStation()).timestamp(s.getTimestamp()).build()).collect(Collectors.toList());
    }

    public List<String> getBlacklist() {
      return blacklistStationsRepository.findAllStations();
    }

    private DateRequest verifyDate(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo != null || dateFrom != null && dateTo == null) {
            throw new AppException(
                    AppError.BAD_REQUEST,
                    "Date from and date to must be both defined");
        } else if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new AppException(
                    AppError.BAD_REQUEST,
                    "Date from must be before date to");
        }
        if (dateFrom == null && dateTo == null) {
            dateTo = LocalDate.now();
            dateFrom = dateTo.minusDays(dateRangeLimit);
        }
        if (ChronoUnit.DAYS.between(dateFrom, dateTo) > dateRangeLimit) {
            throw new AppException(
                    AppError.INTERVAL_TOO_LARGE,
                    dateRangeLimit);
        }
        return DateRequest.builder().from(dateFrom).to(dateTo).build();
    }

}
