package com.example.flight_booking_api.model;

public record Flight(String flightNumber, int capacity, int bookedSeats) {
    public Flight withBookedSeats(int newBookedSeats) {
        return new Flight(flightNumber, capacity, newBookedSeats);
    }
}
