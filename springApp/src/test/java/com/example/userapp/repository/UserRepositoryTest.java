package com.example.userapp.repository;

import com.example.userapp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan(
    basePackages = "com.example.userapp",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.example.userapp.controller.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.example.userapp.config.ObservabilityConfig"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.example.userapp.service.*")
    }
)
@TestPropertySource(properties = {
    "spring.datasource.hikari.initialization-fail-timeout=0"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("john@example.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        
        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByEmail() {
        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("jane@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
