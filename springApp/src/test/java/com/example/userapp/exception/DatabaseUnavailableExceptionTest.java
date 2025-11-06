package com.example.userapp.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseUnavailableExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Database is down";
        DatabaseUnavailableException exception = new DatabaseUnavailableException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Database connection failed";
        Throwable cause = new RuntimeException("Connection timeout");
        DatabaseUnavailableException exception = new DatabaseUnavailableException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getCause());
    }
}
