package com.example.userapp.exception;

/**
 * Runtime exception thrown when a DB-backed operation is attempted but the DB is unavailable.
 */
public class DatabaseUnavailableException extends RuntimeException {

    public DatabaseUnavailableException() {
        super("Database is unavailable");
    }

    public DatabaseUnavailableException(String message) {
        super(message);
    }

    public DatabaseUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
