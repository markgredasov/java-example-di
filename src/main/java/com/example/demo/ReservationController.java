package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class ReservationController {

  private final ReservationService reservationService;
  private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

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

    sendToInternalService(createdReservation, traceID);

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

  private void sendToInternalService(Reservation reservation, String traceID) {
    try {
      String internalServiceURL = getInternalServiceURL();

      var headers = getHttpHeaders(traceID);

      HttpEntity<Reservation> request = new HttpEntity<>(reservation, headers);
      ResponseEntity<String> response = restTemplate.exchange(internalServiceURL, HttpMethod.POST, request, String.class);

      LogSendToInternalServiceResult(response);
    } catch (Exception e) {
      log.error("failed to send request to internal service", e);
    }
  }

  private String getInternalServiceURL() {
    return "http://localhost:8081/internal/log";
  }

  private HttpHeaders getHttpHeaders(String traceID) {
    final String TRACE_ID_HEADER = "X-Trace-ID";

    HttpHeaders headers = new HttpHeaders();
    headers.set(TRACE_ID_HEADER, traceID);
    headers.setContentType(MediaType.APPLICATION_JSON);

    return headers;
  }

  private void LogSendToInternalServiceResult(ResponseEntity<String> response) {
    if (response.getStatusCode().is2xxSuccessful()) {
      log.info("reservation sent to internal service successfully | response: {}", response.getBody());
    } else {
      log.warn("internal service returned: {}", response.getStatusCode());
    }
  }
}
