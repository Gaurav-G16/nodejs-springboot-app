package com.example.userapp.service;

import com.example.userapp.entity.User;
import com.example.userapp.repository.UserRepository;
import com.example.userapp.config.DatabaseAvailability;
import com.example.userapp.exception.DatabaseUnavailableException;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for User operations.
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Counter userRegistrationCounter;
    private final DatabaseAvailability databaseAvailability;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository the user repository
     * @param userRegistrationCounter the user registration counter
     * @param databaseAvailability the database availability checker (optional)
     */
    @Autowired
    public UserService(final UserRepository userRepository,
                       final Counter userRegistrationCounter,
                       @Autowired(required = false) final DatabaseAvailability databaseAvailability) {
        this.userRepository = userRepository;
        this.userRegistrationCounter = userRegistrationCounter;
        this.databaseAvailability = databaseAvailability;
    }

    private void ensureDatabaseUp() {
        if (databaseAvailability != null && !databaseAvailability.isDatabaseUp()) {
            throw new DatabaseUnavailableException("Database is currently unreachable");
        }
    }

    /**
     * Register a new user.
     *
     * @param user the user to register
     * @return the registered user
     * @throws IllegalArgumentException if user already exists
     */
    @Transactional
    public User registerUser(final User user) {
        LOGGER.info("Attempting to register user with email: {}", user.getEmail());

        ensureDatabaseUp();

        if (userRepository.existsByEmail(user.getEmail())) {
            final String errorMsg = "User with email " + user.getEmail() + " already exists";
            LOGGER.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            final User savedUser = userRepository.save(user);
            userRegistrationCounter.increment();
            LOGGER.info("Successfully registered user with ID: {} and email: {}",
                    savedUser.getId(), savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            LOGGER.error("Failed to register user with email: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    /**
     * Get all users.
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        LOGGER.debug("Fetching all users");
        ensureDatabaseUp();
        return getAllUsersInternal();
    }

    @Transactional(readOnly = true)
    private List<User> getAllUsersInternal() {
        final List<User> users = userRepository.findAll();
        LOGGER.debug("Found {} users", users.size());
        return users;
    }

    /**
     * Find user by ID.
     *
     * @param id the user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(final Long id) {
        LOGGER.debug("Finding user by ID: {}", id);
        ensureDatabaseUp();
        return findByIdInternal(id);
    }

    @Transactional(readOnly = true)
    private Optional<User> findByIdInternal(final Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by email.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(final String email) {
        LOGGER.debug("Finding user by email: {}", email);
        ensureDatabaseUp();
        return findByEmailInternal(email);
    }

    @Transactional(readOnly = true)
    private Optional<User> findByEmailInternal(final String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get total user count.
     *
     * @return total number of users
     */
    public long getTotalUserCount() {
        ensureDatabaseUp();
        return getTotalUserCountInternal();
    }

    @Transactional(readOnly = true)
    private long getTotalUserCountInternal() {
        final long count = userRepository.countTotalUsers();
        LOGGER.debug("Total user count: {}", count);
        return count;
    }

    /**
     * Delete user by ID.
     *
     * @param id the user ID to delete
     * @return true if user was deleted, false if not found
     */
    @Transactional
    public boolean deleteUser(final Long id) {
        LOGGER.info("Attempting to delete user with ID: {}", id);
        ensureDatabaseUp();

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            LOGGER.info("Successfully deleted user with ID: {}", id);
            return true;
        } else {
            LOGGER.warn("User with ID {} not found for deletion", id);
            return false;
        }
    }
}
