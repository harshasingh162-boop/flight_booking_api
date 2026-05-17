package com.example.flight_booking_api.controller;

import com.example.flight_booking_api.model.Booking;
import com.example.flight_booking_api.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    public ResponseEntity<Void> registerFlight(@RequestBody FlightRequest flightRequest) {
        flightService.registerFlight(flightRequest.flightNumber(), flightRequest.capacity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{flightNumber}/bookings")
    public ResponseEntity<BookingResponse> bookSeat(@PathVariable String flightNumber, @RequestBody BookingRequest bookingRequest) {
        return flightService.bookSeat(flightNumber, bookingRequest.passengerName())
                .map(booking -> ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(booking.bookingId(), booking.flightNumber())))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    public record FlightRequest(String flightNumber, int capacity) {}
    public record BookingRequest(String passengerName) {}
    public record BookingResponse(String bookingId, String flightNumber) {}
}
