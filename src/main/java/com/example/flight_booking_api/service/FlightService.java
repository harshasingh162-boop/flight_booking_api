package com.example.flight_booking_api.service;

import com.example.flight_booking_api.model.Booking;
import com.example.flight_booking_api.model.Flight;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FlightService {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();

    public void registerFlight(String flightNumber, int capacity) {
        flights.put(flightNumber, new Flight(flightNumber, capacity, 0));
    }

    public Optional<Booking> bookSeat(String flightNumber, String passengerName) {
        AtomicReference<Booking> bookingRef = new AtomicReference<>();
        
        // We use ConcurrentHashMap.compute to ensure that the "check capacity and reserve a seat" 
        // operation is atomic per flight. This prevents race conditions where multiple 
        // concurrent requests could overbook the flight.
        flights.compute(flightNumber, (key, flight) -> {
            if (flight == null || flight.bookedSeats() >= flight.capacity()) {
                // If flight not found or full, return the current state (no change)
                return flight;
            }

            // Create booking and update flight state within the atomic compute block
            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(bookingId, flightNumber, passengerName);
            bookingRef.set(booking);
            
            return flight.withBookedSeats(flight.bookedSeats() + 1);
        });

        return Optional.ofNullable(bookingRef.get());
    }
}
