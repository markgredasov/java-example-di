package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {

  private final ReservationService reservationService;
  private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

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
  public Reservation createReservation(
          @RequestBody Reservation reservationToCreate
  ) {
    log.info("create reservation called");

    return reservationService.createReservation(reservationToCreate);
  }
}
