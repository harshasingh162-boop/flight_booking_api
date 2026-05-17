package com.example.flight_booking_api.service;

import com.example.flight_booking_api.model.Booking;
import com.example.flight_booking_api.model.Flight;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlightService {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();

    public void registerFlight(String flightNumber, int capacity) {
        flights.put(flightNumber, new Flight(flightNumber, capacity, 0));
    }

    public Optional<Booking> bookSeat(String flightNumber, String passengerName) {
        Flight flight = flights.get(flightNumber);
        if (flight == null || flight.bookedSeats() >= flight.capacity()) {
            return Optional.empty();
        }

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, flightNumber, passengerName);
        
        flights.put(flightNumber, flight.withBookedSeats(flight.bookedSeats() + 1));
        
        return Optional.of(booking);
    }
}
