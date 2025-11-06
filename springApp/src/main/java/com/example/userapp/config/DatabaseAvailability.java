package com.example.userapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper component to check whether the database is currently reachable.
 */
@Component
@ConditionalOnBean(DataSource.class)
public class DatabaseAvailability {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAvailability.class);

    private final DataSource dataSource;

    public DatabaseAvailability(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Quickly check if the DB is reachable by trying to obtain a connection.
     * This uses a short-lived connection attempt and returns true/false.
     */
    public boolean isDatabaseUp() {
        try (Connection ignored = dataSource.getConnection()) {
            return true;
        } catch (SQLException e) {
            LOGGER.warn("Database appears to be down: {}", e.getMessage());
            return false;
        }
    }
}
