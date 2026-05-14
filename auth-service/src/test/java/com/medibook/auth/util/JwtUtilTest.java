package com.medibook.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void generateTokenCreatesCompactJwt() {
        JwtUtil jwtUtil = new JwtUtil();

        String token = jwtUtil.generateToken("patient@example.com", "PATIENT");

        assertNotNull(token);
        assertTrue(token.length() > 40);
        assertEquals(3, token.split("\\.").length);
    }
}
