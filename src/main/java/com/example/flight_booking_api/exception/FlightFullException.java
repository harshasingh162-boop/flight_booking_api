package com.example.flight_booking_api.exception;

public class FlightFullException extends RuntimeException {
    public FlightFullException(String message) {
        super(message);
    }
}
