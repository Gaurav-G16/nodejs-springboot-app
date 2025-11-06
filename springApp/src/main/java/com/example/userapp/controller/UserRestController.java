package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.userapp.exception.DatabaseUnavailableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for User operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;

    /**
     * Constructor with dependency injection.
     *
     * @param userService the user service
     */
    @Autowired
    public UserRestController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user.
     *
     * @param user the user to register
     * @return ResponseEntity with the registered user or error
     */
    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody final User user) {
        try {
            final User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("User registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (DatabaseUnavailableException e) {
            LOGGER.warn("Database unavailable during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Database is unavailable"));
        } catch (Exception e) {
            LOGGER.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get all users.
     *
     * @return ResponseEntity with list of users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            final List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (DatabaseUnavailableException e) {
            LOGGER.warn("Database unavailable when fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Get user by ID.
     *
     * @param id the user ID
     * @return ResponseEntity with user or not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable final Long id) {
        try {
            final Optional<User> user = userService.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with ID: " + id));
            }
        } catch (DatabaseUnavailableException e) {
            LOGGER.warn("Database unavailable when looking up user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Database is unavailable"));
        }
    }

    /**
     * Delete user by ID.
     *
     * @param id the user ID to delete
     * @return ResponseEntity with success or not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable final Long id) {
        try {
            final boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with ID: " + id));
            }
        } catch (DatabaseUnavailableException e) {
            LOGGER.warn("Database unavailable when deleting user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Database is unavailable"));
        }
    }

    /**
     * Get user statistics.
     *
     * @return ResponseEntity with user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            final long totalUsers = userService.getTotalUserCount();
            return ResponseEntity.ok(Map.of(
                    "totalUsers", totalUsers,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (DatabaseUnavailableException e) {
            LOGGER.warn("Database unavailable when fetching stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Database is unavailable"));
        }
    }
}
