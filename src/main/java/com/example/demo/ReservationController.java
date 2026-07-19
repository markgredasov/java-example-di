package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
public class ReservationController {

  private final ReservationService reservationService;
  private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
  private static final int MAX_NUMBER_OF_RETRIES = 5;
  private static final long INITIAL_BACKOFF_MS = 1000;

  @Autowired
  private RestTemplate restTemplate;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping("/reservations/{id}")
  public Reservation getReservationByID(@PathVariable("id") Long id) {
    log.info("get reservation by id called");
    return reservationService.getReservationByID(id);
  }

  @GetMapping("/reservations")
  public List<Reservation> getAllReservations() {
    log.info("get all reservations called");
    return reservationService.getAllReservations();
  }

  @PostMapping("/reservations")
  public Reservation createReservation(@RequestBody Reservation reservationToCreate, HttpServletRequest request) {
    log.info("create reservation called");

    Reservation createdReservation = reservationService.createReservation(reservationToCreate);

    String traceID = getTraceIdFromRequest(request);

    sendToLoggingService(createdReservation, traceID);

    return createdReservation;
  }

  @DeleteMapping("/reservations/{id}")
  public Reservation deleteReservationByID(@PathVariable("id") Long id) {
    log.info("delete reservation by id called");
    return reservationService.deleteReservationByID(id);
  }

  private String getTraceIdFromRequest(HttpServletRequest request) {
    String traceID = (String) request.getAttribute("traceID");
    return traceID == null ? "" : traceID;
  }

  private void sendToLoggingService(Reservation reservation, String traceID) {
    Instant attemptStartTime = Instant.now();

    for (int attempt = 0; attempt < MAX_NUMBER_OF_RETRIES; attempt++) {
      try {
        String internalServiceURL = getLoggingServiceURL();

        var headers = setHttpHeaders(traceID);

        HttpEntity<Reservation> request = new HttpEntity<>(reservation, headers);
        ResponseEntity<String> response = restTemplate.exchange(internalServiceURL, HttpMethod.POST, request, String.class);

        Duration attemptDuration = Duration.between(attemptStartTime, Instant.now());

        logSendToLoggingServiceResult(response, attemptDuration);

        return;

      } catch (Exception e) {
        Duration attemptDuration = Duration.between(attemptStartTime, Instant.now());
        log.warn("attempt {}: failed to send request to logging service after {} ms: {}", attempt, attemptDuration, e.getMessage());

        if (attempt < MAX_NUMBER_OF_RETRIES - 1) {
          long currentBackoff = calculateBackoffInMs(attempt);
          try {
            Thread.sleep(currentBackoff);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("retry was interrupted: {}", ex.getMessage());
            break;
          }
        }
        log.error("failed to send request to internal service", e);
      }
    }
  }

  private String getLoggingServiceURL() {
    return "http://localhost:8081/internal/log";
  }

  private HttpHeaders setHttpHeaders(String traceID) {
    final String TRACE_ID_HEADER = "X-Trace-ID";

    HttpHeaders headers = new HttpHeaders();
    headers.set(TRACE_ID_HEADER, traceID);
    headers.setContentType(MediaType.APPLICATION_JSON);

    return headers;
  }

  private void logSendToLoggingServiceResult(ResponseEntity<String> response, Duration attemptDuration) {
    if (response.getStatusCode().is2xxSuccessful()) {
      log.info("reservation sent to internal service successfully after {} ms: response: {}", attemptDuration, response.getBody());
    } else {
      log.warn("internal service returned: {}", response.getStatusCode());
    }
  }

  private long calculateBackoffInMs(int i) {
    return INITIAL_BACKOFF_MS * (long) Math.pow(2, i);
  }
}
