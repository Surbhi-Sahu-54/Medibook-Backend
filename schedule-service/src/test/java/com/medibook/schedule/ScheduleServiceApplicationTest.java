package com.medibook.schedule;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleServiceApplicationTest {

    @Test
    void applicationClassIsBootstrappable() {
        assertTrue(ScheduleServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
