package it.gov.pagopa.standintechsupport.repository;

import it.gov.pagopa.standintechsupport.repository.entity.StandInStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandInStationsRepository extends JpaRepository<StandInStation, String> {
    @Query("select station from StandInStation")
    public List<String> findAllStations();
}
