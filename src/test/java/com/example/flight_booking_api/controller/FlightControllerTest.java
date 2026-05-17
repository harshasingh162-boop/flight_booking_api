package com.example.flight_booking_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FlightControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void createFlight_returns201() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA100","capacity":100}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void bookExistingFlightWithCapacity_returns201WithBookingId() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA200","capacity":10}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/flights/AA200/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passengerName":"Alice"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.flightNumber").value("AA200"));
    }

    @Test
    void bookNonExistentFlight_returns404() throws Exception {
        mockMvc.perform(post("/flights/UNKNOWN/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passengerName":"Bob"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void blankPassengerName_returns400() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA300","capacity":10}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/flights/AA300/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passengerName":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonPositiveCapacity_returns400() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA400","capacity":0}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA401","capacity":-5}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookBeyondCapacity_returns409AndNeverExceedsCapacity() throws Exception {
        int capacity = 5;
        int totalRequests = 20;

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA500","capacity":%d}
                                """.formatted(capacity)))
                .andExpect(status().isCreated());

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    return mockMvc.perform(post("/flights/AA500/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {"passengerName":"Passenger %d"}
                                            """.formatted(idx)))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        executor.shutdown();

        long successCount = 0;
        long conflictCount = 0;
        for (Future<Integer> f : futures) {
            int status = f.get();
            if (status == 201) successCount++;
            else if (status == 409) conflictCount++;
        }

        assert successCount == capacity
                : "Expected exactly %d successful bookings, got %d".formatted(capacity, successCount);
        assert conflictCount == totalRequests - capacity
                : "Expected %d conflicts, got %d".formatted(totalRequests - capacity, conflictCount);
    }
}
