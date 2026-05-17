package com.example.flight_booking_api.service;

import com.example.flight_booking_api.exception.FlightAlreadyExistsException;
import com.example.flight_booking_api.exception.FlightFullException;
import com.example.flight_booking_api.exception.FlightNotFoundException;
import com.example.flight_booking_api.model.Booking;
import com.example.flight_booking_api.model.Flight;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FlightService {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();

    public void registerFlight(String flightNumber, int capacity) {
        Flight previous = flights.putIfAbsent(flightNumber, new Flight(flightNumber, capacity, 0));
        if (previous != null) {
            throw new FlightAlreadyExistsException("Flight " + flightNumber + " already exists");
        }
    }

    public Booking bookSeat(String flightNumber, String passengerName) {
        AtomicReference<Booking> bookingRef = new AtomicReference<>();
        
        // Use compute to make the check-and-increment operation atomic per flightNumber.
        // We throw exceptions inside the compute block; if an exception is thrown, 
        // the map is not updated for this key.
        flights.compute(flightNumber, (key, flight) -> {
            if (flight == null) {
                throw new FlightNotFoundException("Flight " + flightNumber + " not found");
            }
            if (flight.bookedSeats() >= flight.capacity()) {
                throw new FlightFullException("Flight " + flightNumber + " is full");
            }

            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(bookingId, flightNumber, passengerName);
            bookingRef.set(booking);
            
            return flight.withBookedSeats(flight.bookedSeats() + 1);
        });

        return bookingRef.get();
    }
}
