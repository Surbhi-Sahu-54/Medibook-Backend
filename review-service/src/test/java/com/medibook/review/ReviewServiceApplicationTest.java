package com.medibook.review;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewServiceApplicationTest {

    @Test
    void applicationClassIsBootstrappable() {
        assertTrue(
            ReviewServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)
        );
    }
}