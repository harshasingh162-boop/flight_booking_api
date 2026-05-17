package com.example.flight_booking_api.controller;

import com.example.flight_booking_api.service.FlightService;
import org.junit.jupiter.api.Test;
import com.example.flight_booking_api.model.Booking;
import com.example.flight_booking_api.exception.FlightFullException;

import static org.junit.jupiter.api.Assertions.*;

public class FlightControllerTest {

    @Test
    public void testFlightService() {
        FlightService service = new FlightService();
        service.registerFlight("LH123", 1);
        
        Booking booking1 = service.bookSeat("LH123", "John Doe");
        assertNotNull(booking1);
        assertEquals("LH123", booking1.flightNumber());
        assertEquals("John Doe", booking1.passengerName());
        assertNotNull(booking1.bookingId());

        assertThrows(FlightFullException.class, () -> service.bookSeat("LH123", "Jane Smith"));
    }
}
