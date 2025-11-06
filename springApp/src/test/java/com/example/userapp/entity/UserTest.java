package com.example.userapp.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNotNull(user.getCreatedAt()); // createdAt is set in constructor
    }

    @Test
    void testParameterizedConstructor() {
        User user = new User("John Doe", "john@example.com");
        assertNull(user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        
        user.setId(1L);
        user.setName("Jane Smith");
        user.setEmail("jane@example.com");
        user.setCreatedAt(now);
        
        assertEquals(1L, user.getId());
        assertEquals("Jane Smith", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testCreatedAtSetInConstructor() {
        User user = new User();
        assertNotNull(user.getCreatedAt()); // createdAt is set in constructor
        
        user = new User("John", "john@example.com");
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testToString() {
        User user = new User("John Doe", "john@example.com");
        user.setId(1L);
        
        String toString = user.toString();
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
        assertTrue(toString.contains("id=1"));
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User("John Doe", "john@example.com");
        user1.setId(1L);
        
        User user2 = new User("John Doe", "john@example.com");
        user2.setId(1L);
        
        User user3 = new User("Jane Smith", "jane@example.com");
        user3.setId(2L);
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());
        
        assertNotEquals(user1, null);
        assertNotEquals(user1, "not a user");
    }

    @Test
    void testEqualsWithNullId() {
        User user1 = new User("John Doe", "john@example.com");
        User user2 = new User("John Doe", "john@example.com");
        
        // Users with same email should be equal even with null IDs
        assertEquals(user1, user2);
    }

    @Test
    void testEqualsWithSameObject() {
        User user = new User("John Doe", "john@example.com");
        assertEquals(user, user);
    }
}
