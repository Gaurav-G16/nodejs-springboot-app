package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import com.example.userapp.config.DatabaseAvailability;
import com.example.userapp.exception.DatabaseUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserWebControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DatabaseAvailability databaseAvailability;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    private UserWebController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserWebController(userService, databaseAvailability);
    }

    @Test
    void testShowRegistrationFormWithDatabaseUp() {
        List<User> users = Arrays.asList(new User("John", "john@example.com"));
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(userService.getAllUsers()).thenReturn(users);

        String result = controller.showRegistrationForm(model);

        assertEquals("index", result);
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("users", users);
        verify(model).addAttribute("totalUsers", 1);
    }

    @Test
    void testShowRegistrationFormWithDatabaseDown() {
        when(databaseAvailability.isDatabaseUp()).thenReturn(false);

        String result = controller.showRegistrationForm(model);

        assertEquals("index", result);
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("dbDown", true);
    }

    @Test
    void testShowRegistrationFormWithDatabaseException() {
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(userService.getAllUsers()).thenThrow(new DatabaseUnavailableException("DB error"));

        String result = controller.showRegistrationForm(model);

        assertEquals("index", result);
        verify(model).addAttribute("dbDown", true);
    }

    @Test
    void testShowRegistrationFormWithNullDatabaseAvailability() {
        controller = new UserWebController(userService, null);

        String result = controller.showRegistrationForm(model);

        assertEquals("index", result);
        verify(model).addAttribute("dbDown", true);
    }

    @Test
    void testProcessRegistrationSuccess() {
        User user = new User("John Doe", "john@example.com");
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(user);

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("success", "User registered successfully!");
    }

    @Test
    void testProcessRegistrationWithDatabaseDown() {
        User user = new User("John Doe", "john@example.com");
        when(databaseAvailability.isDatabaseUp()).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Registration currently unavailable: database is down");
    }

    @Test
    void testProcessRegistrationValidationErrors() {
        User user = new User("", "invalid-email");
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Please correct the form errors");
    }

    @Test
    void testProcessRegistrationIllegalArgumentException() {
        User user = new User("John Doe", "john@example.com");
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Email already exists");
    }

    @Test
    void testProcessRegistrationDatabaseUnavailableException() {
        User user = new User("John Doe", "john@example.com");
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(User.class)))
                .thenThrow(new DatabaseUnavailableException("DB down"));

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Registration currently unavailable: database is down");
    }

    @Test
    void testProcessRegistrationUnexpectedException() {
        User user = new User("John Doe", "john@example.com");
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = controller.registerUser(user, bindingResult, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Registration failed. Please try again.");
    }

    @Test
    void testTestEndpoint() {
        String result = controller.test();
        assertEquals("redirect:/", result);
    }

    @Test
    void testTestDeleteEndpoint() {
        String result = controller.testDelete(redirectAttributes);
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("success", "Test delete called!");
    }

    @Test
    void testDeleteUserSuccess() {
        when(userService.deleteUser(1L)).thenReturn(true);

        String result = controller.deleteUser(1L, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("success", "User deleted successfully!");
    }

    @Test
    void testDeleteUserNotFound() {
        when(userService.deleteUser(1L)).thenReturn(false);

        String result = controller.deleteUser(1L, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "User not found with ID: 1");
    }

    @Test
    void testDeleteUserDatabaseUnavailable() {
        when(userService.deleteUser(1L)).thenThrow(new DatabaseUnavailableException("DB down"));

        String result = controller.deleteUser(1L, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Deletion currently unavailable: database is down");
    }

    @Test
    void testDeleteUserException() {
        when(userService.deleteUser(1L)).thenThrow(new RuntimeException("Delete failed"));

        String result = controller.deleteUser(1L, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to delete user: Delete failed");
    }

    @Test
    void testHealthCheck() {
        when(databaseAvailability.isDatabaseUp()).thenReturn(true);

        String result = controller.healthCheck(model);

        assertEquals("health", result);
        verify(model).addAttribute("status", "UP");
        verify(model).addAttribute("dbUp", true);
        verify(model).addAttribute(eq("timestamp"), any(Long.class));
    }

    @Test
    void testHealthCheckDatabaseDown() {
        when(databaseAvailability.isDatabaseUp()).thenReturn(false);

        String result = controller.healthCheck(model);

        assertEquals("health", result);
        verify(model).addAttribute("status", "DOWN");
        verify(model).addAttribute("dbUp", false);
    }

    @Test
    void testHealthCheckWithNullDatabaseAvailability() {
        controller = new UserWebController(userService, null);

        String result = controller.healthCheck(model);

        assertEquals("health", result);
        verify(model).addAttribute("status", "DOWN");
        verify(model).addAttribute("dbUp", false);
    }
}
