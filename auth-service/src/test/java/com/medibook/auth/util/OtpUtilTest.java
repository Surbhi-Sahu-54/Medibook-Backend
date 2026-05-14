package com.medibook.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpUtilTest {

    @Test
    void generateOtpReturnsSixDigits() {
        assertTrue(OtpUtil.generateOtp().matches("\\d{6}"));
    }
}
