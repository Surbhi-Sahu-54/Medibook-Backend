package com.medibook.record;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordServiceApplicationTest {

    @Test
    void applicationClassIsBootstrappable() {
        assertTrue(RecordServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
