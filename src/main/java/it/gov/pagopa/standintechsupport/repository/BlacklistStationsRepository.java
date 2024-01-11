package it.gov.pagopa.standintechsupport.repository;

import it.gov.pagopa.standintechsupport.repository.entity.BlacklistStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlacklistStationsRepository extends JpaRepository<BlacklistStation, String> {

  @Query("select station from BlacklistStation")
  public List<String> findAllStations();
}
