package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MetricsControllerTest {

    private SimpleMeterRegistry registry;
    private UserService userService;
    private MetricsController controller;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        userService = Mockito.mock(UserService.class);
        controller = new MetricsController(registry, userService);
    }

    @Test
    void getMetrics_returnsExpectedKeys() {
        when(userService.getAllUsers()).thenReturn(List.of(new User("A","a@a"), new User("B","b@b")));

        // register some counters/gauges
        Counter.builder("users.registered.total").register(registry).increment(2.0);
        Gauge.builder("hikaricp.connections.active", () -> 3.0).register(registry);

        Map<String, Object> metrics = controller.getMetrics();
        assertThat(metrics).containsKeys(
                "userapp_users_registered_total",
                "userapp_users_registration_attempts",
                "userapp_db_connections_active"
        );
        assertThat(((Double)metrics.get("userapp_users_registered_total"))).isEqualTo(2.0);
    }
}
