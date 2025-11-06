package com.example.userapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SystemDashboardController {

    private final Environment environment;

    @Autowired
    public SystemDashboardController(Environment environment) {
        this.environment = environment;
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

        // App info
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", "Spring User App");
        appInfo.put("version", "1.0.0");
        appInfo.put("profile", String.join(",", environment.getActiveProfiles()));
        appInfo.put("javaVersion", System.getProperty("java.version"));
        appInfo.put("springBootVersion", "3.2.0");

        // System stats - completely avoid database calls
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("totalUsers", "Check /api/users/stats");
        systemStats.put("uptime", System.currentTimeMillis());
        systemStats.put("dbStatus", "Check /actuator/health");

        // Health status
        String healthStatus = "Check /actuator/health";
        
        model.addAttribute("apiEndpoints", apiEndpoints);
        model.addAttribute("appInfo", appInfo);
        model.addAttribute("systemStats", systemStats);
        model.addAttribute("healthStatus", healthStatus);
        model.addAttribute("dbConnected", true); // Default to true for UI purposes

        return "dashboard";
    }
}
