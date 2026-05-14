package com.medibook.user.repository;

import com.medibook.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsUserByEmail() {
        userRepository.save(User.builder()
                .email("repo@example.com")
                .password("secret")
                .role("PATIENT")
                .firstName("Repo")
                .lastName("User")
                .build());

        assertTrue(userRepository.findByEmail("repo@example.com").isPresent());
    }
}
