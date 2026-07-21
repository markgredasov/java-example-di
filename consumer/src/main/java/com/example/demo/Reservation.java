package com.example.demo;

import java.time.LocalDate;

public record Reservation (
        Long id,
        Long userID,
        Long roomID,
        LocalDate startDate,
        LocalDate endDate,
        ReservationStatus status
){
}
