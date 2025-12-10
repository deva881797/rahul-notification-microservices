package com.example.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
class OtpServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    // -----------------------------------------------------
    // TEST 1: generateOtp() should generate and store OTP
    // -----------------------------------------------------
    @Test
    void generateOtp_shouldStoreOtpInRedis() {

        String email = "test@gmail.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String otp = otpService.generateOtp(email);

        assertNotNull(otp);
        assertEquals(6, otp.length());

        verify(valueOperations).set(eq("OTP_" + email), eq(otp), eq(5L), eq(TimeUnit.MINUTES));
    }
    // -----------------------------------------------------
    // TEST 2: verifyOtp() should return TRUE when OTP matches
    // -----------------------------------------------------
    @Test
    void verifyOtp_shouldReturnTrue_whenOtpMatches() {

        String email = "test@gmail.com";
        String otp = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("OTP_" + email)).thenReturn("123456");

        boolean result = otpService.verifyOtp(email, otp);

        assertTrue(result);
        verify(redisTemplate).delete("OTP_" + email);
    }

    // -----------------------------------------------------
    // TEST 3: verifyOtp() should return FALSE when OTP does NOT match
    // -----------------------------------------------------
    @Test
    void verifyOtp_shouldReturnFalse_whenOtpDoesNotMatch() {

        String email = "test@gmail.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("OTP_" + email)).thenReturn("111111");

        boolean result = otpService.verifyOtp(email, "999999");

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    // -----------------------------------------------------
    // TEST 4: verifyOtp() returns FALSE when stored OTP is null
    // -----------------------------------------------------
    @Test
    void verifyOtp_shouldReturnFalse_whenStoredOtpIsNull() {

        String email = "test@gmail.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("OTP_" + email)).thenReturn(null);

        boolean result = otpService.verifyOtp(email, "123456");

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }
}
