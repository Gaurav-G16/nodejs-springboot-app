package com.example.userapp.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DatabaseAvailabilityTest {

    @Test
    void isDatabaseUp_trueWhenConnectionObtained() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        DatabaseAvailability availability = new DatabaseAvailability(ds);
        assertThat(availability.isDatabaseUp()).isTrue();
    }

    @Test
    void isDatabaseUp_falseWhenSQLException() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new SQLException("boom"));

        DatabaseAvailability availability = new DatabaseAvailability(ds);
        assertThat(availability.isDatabaseUp()).isFalse();
    }
}
