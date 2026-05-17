package com.example.flight_booking_api;

import com.example.flight_booking_api.service.FlightService;
import com.example.flight_booking_api.exception.FlightFullException;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyTest {

    @Test
    public void testConcurrentBooking() throws InterruptedException {
        FlightService service = new FlightService();
        String flightNumber = "CONC123";
        int capacity = 10;
        service.registerFlight(flightNumber, capacity);

        int numThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        AtomicInteger successfulBookings = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final String passengerName = "Passenger " + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    try {
                        service.bookSeat(flightNumber, passengerName);
                        successfulBookings.incrementAndGet();
                    } catch (FlightFullException e) {
                        // Expected when full
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(capacity, successfulBookings.get(),
                "Expected exactly " + capacity + " successful bookings, got " + successfulBookings.get());
    }
}
