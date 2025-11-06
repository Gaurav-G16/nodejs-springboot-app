package com.example.userapp.controller;

import com.example.userapp.entity.User;
import com.example.userapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser_Success() throws Exception {
        // Given
        final User user = new User("John Doe", "john@example.com");
        final User savedUser = new User("John Doe", "john@example.com");
        savedUser.setId(1L);

        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Given
        final User user = new User("John Doe", "john@example.com");
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("User with email john@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with email john@example.com already exists"));
    }

    @Test
    void testRegisterUser_ValidationError() throws Exception {
        // Given
        final User user = new User("", "invalid-email");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Given
        final List<User> users = Arrays.asList(
                new User("John Doe", "john@example.com"),
                new User("Jane Smith", "jane@example.com")
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void testGetUserById_UserExists() throws Exception {
        // Given
        final User user = new User("John Doe", "john@example.com");
        user.setId(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUserById_UserNotExists() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with ID: 1"));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void testDeleteUser_UserNotExists() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with ID: 1"));
    }

    @Test
    void testGetUserStats() throws Exception {
        // Given
        when(userService.getTotalUserCount()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(5L))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
