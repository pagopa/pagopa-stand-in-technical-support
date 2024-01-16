package it.gov.pagopa.standintechsupport.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppError {
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid Request", "%s"),
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something was wrong"),
  INTERVAL_TOO_LARGE(HttpStatus.BAD_REQUEST, "Date interval too large", "%s");

  public final HttpStatus httpStatus;
  public final String title;
  public final String details;

  AppError(HttpStatus httpStatus, String title, String details) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.details = details;
  }
}
