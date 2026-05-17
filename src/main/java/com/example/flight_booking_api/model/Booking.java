package com.example.flight_booking_api.model;

import jakarta.validation.constraints.NotBlank;

public record Booking(
    @NotBlank String bookingId, 
    @NotBlank String flightNumber, 
    @NotBlank String passengerName
) {}
