package com.example.userapp.controller;

import com.example.userapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SystemDashboardController {

    private final UserService userService;
    private final DataSource dataSource;
    private final Environment environment;
    private final HealthEndpoint healthEndpoint;

    @Autowired
    public SystemDashboardController(UserService userService, DataSource dataSource, 
                                   Environment environment, HealthEndpoint healthEndpoint) {
        this.userService = userService;
        this.dataSource = dataSource;
        this.environment = environment;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // API endpoints
        Map<String, String> apiEndpoints = new HashMap<>();
        apiEndpoints.put("POST /api/users", "Register new user");
        apiEndpoints.put("GET /api/users", "List all users");
        apiEndpoints.put("GET /api/users/{id}", "Get user by ID");
        apiEndpoints.put("DELETE /api/users/{id}", "Delete user");
        apiEndpoints.put("GET /api/users/stats", "User statistics");
        apiEndpoints.put("GET /actuator/health", "Health check");
        apiEndpoints.put("GET /actuator/prometheus", "Prometheus metrics");
        apiEndpoints.put("GET /metrics", "Custom metrics");

        // Database status
        boolean dbConnected = checkDatabaseConnection();
        
        // App info
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", "Spring User App");
        appInfo.put("version", "1.0.0");
        appInfo.put("profile", String.join(",", environment.getActiveProfiles()));
        appInfo.put("javaVersion", System.getProperty("java.version"));
        appInfo.put("springBootVersion", "3.2.0");

        // System stats
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("totalUsers", userService.getAllUsers().size());
        systemStats.put("uptime", System.currentTimeMillis());
        systemStats.put("dbStatus", dbConnected ? "Connected" : "Disconnected");

        // Health status
        var health = healthEndpoint.health();
        
        model.addAttribute("apiEndpoints", apiEndpoints);
        model.addAttribute("appInfo", appInfo);
        model.addAttribute("systemStats", systemStats);
        model.addAttribute("healthStatus", health.getStatus().getCode());
        model.addAttribute("dbConnected", dbConnected);

        return "dashboard";
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
}
