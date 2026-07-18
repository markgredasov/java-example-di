package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {
  private final Map<Long, Reservation> reservationMap;
  private final AtomicLong idCounter;

  public ReservationService() {
    reservationMap = new HashMap<>();
    idCounter = new AtomicLong();
  }

  public Reservation getReservationByID(Long id) {
    if (!reservationMap.containsKey(id)) {
      throw new NoSuchElementException("not found reservation by id = " + id);
    }

    return reservationMap.get(id);
  }

  public List<Reservation> getAllReservations() {
    return reservationMap.values().stream().toList();
  }

  public Reservation createReservation(Reservation reservationToCreate) {
    if (reservationToCreate.id() != null) {
      throw new IllegalArgumentException("id should be empty");
    }

    if (reservationToCreate.status() != null) {
      throw new IllegalArgumentException("status should be empty");
    }

    var newReservation = new Reservation(
            idCounter.incrementAndGet(),
            reservationToCreate.userID(),
            reservationToCreate.roomID(),
            reservationToCreate.startDate(),
            reservationToCreate.endDate(),
            ReservationStatus.CREATED
    );

    reservationMap.put(newReservation.id(), newReservation);

    return newReservation;
  }

  public Reservation deleteReservationByID(Long id) {
    if (!reservationMap.containsKey(id)) {
      throw new NoSuchElementException("reservation with id = " + id + " was not found");
    }

    var deletedReservation = reservationMap.get(id);
    reservationMap.remove(id);

    return deletedReservation;
  }
}
