package com.example.flight_booking_api.controller;

import com.example.flight_booking_api.service.FlightService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import com.example.flight_booking_api.model.Booking;

import static org.junit.jupiter.api.Assertions.*;

public class FlightControllerTest {

    @Test
    public void testFlightService() {
        FlightService service = new FlightService();
        service.registerFlight("LH123", 1);
        
        Optional<Booking> booking1 = service.bookSeat("LH123", "John Doe");
        assertTrue(booking1.isPresent());
        assertEquals("LH123", booking1.get().flightNumber());
        assertEquals("John Doe", booking1.get().passengerName());
        assertNotNull(booking1.get().bookingId());

        Optional<Booking> booking2 = service.bookSeat("LH123", "Jane Smith");
        assertFalse(booking2.isPresent());
    }
}
