package com.medibook.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceApplicationTest {

    @Test
    void applicationClassIsBootstrappable() {
        assertTrue(
            PaymentServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)
        );
    }
}