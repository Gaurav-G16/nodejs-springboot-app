package com.example.userapp.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseStartupConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        String url = environment.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/userapp");
        String username = environment.getProperty("spring.datasource.username", "postgres");
        String password = environment.getProperty("spring.datasource.password", "password");
        
        boolean dbAvailable = isDatabaseAvailable(url, username, password);
        
        Map<String, Object> properties = new HashMap<>();
        if (!dbAvailable) {
            // Disable JPA entirely when database is not available
            properties.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration");
            properties.put("spring.jpa.enabled", "false");
        }
        
        MapPropertySource propertySource = new MapPropertySource("databaseAvailability", properties);
        environment.getPropertySources().addFirst(propertySource);
    }
    
    private boolean isDatabaseAvailable(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            return connection.isValid(2);
        } catch (Exception e) {
            System.out.println("Database not available: " + e.getMessage());
            return false;
        }
    }
}
