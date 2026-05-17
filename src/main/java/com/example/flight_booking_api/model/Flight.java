package com.example.flight_booking_api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record Flight(
    @NotBlank String flightNumber, 
    @Positive int capacity, 
    @Min(0) int bookedSeats
) {
    public Flight withBookedSeats(int newBookedSeats) {
        return new Flight(flightNumber, capacity, newBookedSeats);
    }
}
