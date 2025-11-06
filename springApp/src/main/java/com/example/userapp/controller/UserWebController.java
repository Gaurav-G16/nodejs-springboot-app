package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import com.example.userapp.config.DatabaseAvailability;
import com.example.userapp.exception.DatabaseUnavailableException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web controller for User UI operations.
 */
@Controller
public class UserWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserWebController.class);

    private final UserService userService;
    private final DatabaseAvailability databaseAvailability;

    /**
     * Constructor with dependency injection.
     *
     * @param userService the user service
     * @param databaseAvailability the database availability checker (optional)
     */
    @Autowired
    public UserWebController(final UserService userService,
                             @Autowired(required = false) final DatabaseAvailability databaseAvailability) {
        this.userService = userService;
        this.databaseAvailability = databaseAvailability;
        LOGGER.info("UserWebController initialized with delete mapping");
    }

    /**
     * Show the user registration form.
     *
     * @param model the model
     * @return the registration view
     */
    @GetMapping("/")
    public String showRegistrationForm(final Model model) {
        model.addAttribute("user", new User());
        if (databaseAvailability != null && databaseAvailability.isDatabaseUp()) {
            try {
                final List<User> users = userService.getAllUsers();
                model.addAttribute("users", users);
                model.addAttribute("totalUsers", users.size());
            } catch (DatabaseUnavailableException e) {
                model.addAttribute("users", List.of());
                model.addAttribute("totalUsers", 0);
                model.addAttribute("dbDown", true);
            }
        } else {
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("dbDown", true);
        }
        
        // Add dashboard data
        addDashboardData(model);
        
        return "index";
    }
    
    private void addDashboardData(final Model model) {
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
        appInfo.put("javaVersion", System.getProperty("java.version"));
        appInfo.put("springBootVersion", "3.2.0");

        // System stats
        Map<String, Object> systemStats = new HashMap<>();
        boolean dbConnected = databaseAvailability != null && databaseAvailability.isDatabaseUp();
        systemStats.put("dbStatus", dbConnected ? "Connected" : "Disconnected");
        
        model.addAttribute("apiEndpoints", apiEndpoints);
        model.addAttribute("appInfo", appInfo);
        model.addAttribute("systemStats", systemStats);
        model.addAttribute("healthStatus", dbConnected ? "UP" : "DOWN");
        model.addAttribute("dbConnected", dbConnected);
    }

    /**
     * Handle user registration form submission.
     *
     * @param user               the user to register
     * @param bindingResult      validation results
     * @param redirectAttributes redirect attributes for flash messages
     * @return redirect to home page
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute final User user,
                               final BindingResult bindingResult,
                               final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            LOGGER.warn("User registration failed due to validation errors");
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors");
            return "redirect:/";
        }

        if (databaseAvailability != null && !databaseAvailability.isDatabaseUp()) {
            redirectAttributes.addFlashAttribute("error", "Registration currently unavailable: database is down");
            LOGGER.warn("Attempted registration while DB is down: {}", user.getEmail());
            return "redirect:/";
        }

        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "User registered successfully!");
            LOGGER.info("User registered successfully via web form: {}", user.getEmail());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            LOGGER.warn("User registration failed: {}", e.getMessage());
        } catch (DatabaseUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", "Registration currently unavailable: database is down");
            LOGGER.warn("Database unavailable during registration: {}", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Registration failed. Please try again.");
            LOGGER.error("Unexpected error during user registration", e);
        }

        return "redirect:/";
    }

    /**
     * Test endpoint to verify controller is working.
     */
    @GetMapping("/test")
    public String test() {
        LOGGER.info("Test endpoint called");
        return "redirect:/";
    }

    /**
     * Simple test delete endpoint.
     */
    @PostMapping("/testdelete")
    public String testDelete(final RedirectAttributes redirectAttributes) {
        LOGGER.info("Test delete endpoint called");
        redirectAttributes.addFlashAttribute("success", "Test delete called!");
        return "redirect:/";
    }

    /**
     * Delete user via web form.
     *
     * @param id the user ID to delete
     * @param redirectAttributes redirect attributes for flash messages
     * @return redirect to home page
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable final Long id, 
                             final RedirectAttributes redirectAttributes) {
        try {
            final boolean deleted = userService.deleteUser(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
                LOGGER.info("User deleted successfully via web form: {}", id);
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found with ID: " + id);
                LOGGER.warn("Attempted to delete non-existent user via web form: {}", id);
            }
        } catch (DatabaseUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", "Deletion currently unavailable: database is down");
            LOGGER.warn("Database unavailable during user delete via web form: {}: {}", id, e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
            LOGGER.error("Failed to delete user via web form: {}", id, e);
        }
        return "redirect:/";
    }

    /**
     * Health check endpoint.
     *
     * @param model the model
     * @return health check view
     */
    @GetMapping("/health")
    public String healthCheck(final Model model) {
        final boolean dbUp = databaseAvailability != null && databaseAvailability.isDatabaseUp();
        model.addAttribute("status", dbUp ? "UP" : "DOWN");
        model.addAttribute("dbUp", dbUp);
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "health";
    }
}
