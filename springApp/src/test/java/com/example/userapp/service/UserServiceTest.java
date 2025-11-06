package com.example.userapp.service;

import com.example.userapp.entity.User;
import com.example.userapp.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private Counter userRegistrationCounter;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRegistrationCounter = Counter.builder("user.registrations")
                .description("Number of user registrations")
                .register(new SimpleMeterRegistry());
        userService = new UserService(userRepository, userRegistrationCounter);
    }

    @Test
    void testRegisterUser_Success() {
        // Given
        final User user = new User("John Doe", "john@example.com");
        final User savedUser = new User("John Doe", "john@example.com");
        savedUser.setId(1L);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        final User result = userService.registerUser(user);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(user);

        // Verify metrics
        assertEquals(1.0, userRegistrationCounter.count());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Given
        final User user = new User("John Doe", "john@example.com");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(user)
        );

        assertEquals("User with email john@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void testGetAllUsers() {
        // Given
        final List<User> users = Arrays.asList(
                new User("John Doe", "john@example.com"),
                new User("Jane Smith", "jane@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        final List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    void testFindById_UserExists() {
        // Given
        final User user = new User("John Doe", "john@example.com");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        final Optional<User> result = userService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindById_UserNotExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        final Optional<User> result = userService.findById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindByEmail() {
        // Given
        final User user = new User("John Doe", "john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        final Optional<User> result = userService.findByEmail("john@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void testGetTotalUserCount() {
        // Given
        when(userRepository.countTotalUsers()).thenReturn(5L);

        // When
        final long result = userService.getTotalUserCount();

        // Then
        assertEquals(5L, result);
        verify(userRepository).countTotalUsers();
    }

    @Test
    void testDeleteUser_UserExists() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        final boolean result = userService.deleteUser(1L);

        // Then
        assertTrue(result);
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_UserNotExists() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When
        final boolean result = userService.deleteUser(1L);

        // Then
        assertFalse(result);
        verify(userRepository).existsById(1L);
    }
}
