package com.example.userapp.service;

import com.example.userapp.entity.User;
import com.example.userapp.repository.UserRepository;
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
@Transactional
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Counter userRegistrationCounter;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository the user repository
     * @param userRegistrationCounter the user registration counter
     */
    @Autowired
    public UserService(final UserRepository userRepository, final Counter userRegistrationCounter) {
        this.userRepository = userRepository;
        this.userRegistrationCounter = userRegistrationCounter;
    }

    /**
     * Register a new user.
     *
     * @param user the user to register
     * @return the registered user
     * @throws IllegalArgumentException if user already exists
     */
    public User registerUser(final User user) {
        LOGGER.info("Attempting to register user with email: {}", user.getEmail());

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
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        LOGGER.debug("Fetching all users");
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
    @Transactional(readOnly = true)
    public Optional<User> findById(final Long id) {
        LOGGER.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Find user by email.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(final String email) {
        LOGGER.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Get total user count.
     *
     * @return total number of users
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
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
    public boolean deleteUser(final Long id) {
        LOGGER.info("Attempting to delete user with ID: {}", id);
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
