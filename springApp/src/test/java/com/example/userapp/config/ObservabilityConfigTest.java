package com.example.userapp.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObservabilityConfigTest {

    @Test
    void testUserRegistrationCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObservabilityConfig config = new ObservabilityConfig();
        
        Counter counter = config.userRegistrationCounter(meterRegistry);
        
        assertNotNull(counter);
        assertEquals("user_registrations_total", counter.getId().getName());
        assertEquals("Total number of user registrations", counter.getId().getDescription());
        assertEquals(0.0, counter.count());
        
        counter.increment();
        assertEquals(1.0, counter.count());
    }
}
