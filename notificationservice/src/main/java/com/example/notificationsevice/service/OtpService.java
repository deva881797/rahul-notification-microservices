package com.example.notificationsevice.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_EXPIRATION_MINUTES = 5;

    private static class OtpData {
        String otp;
        Instant timestamp;

        OtpData(String otp, Instant timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(900000) + 100000);
        otpStore.put(email, new OtpData(otp, Instant.now()));
        return otp;
    }

    public boolean verifyOtp(String email, String userOtp) {
        OtpData data = otpStore.get(email);
        if (data == null) return false;

        boolean expired = Duration.between(data.timestamp, Instant.now())
                .toMinutes() >= OTP_EXPIRATION_MINUTES;

        if (expired || !data.otp.equals(userOtp)) {
            otpStore.remove(email);
            return false;
        }

        otpStore.remove(email);
        return true;
    }
}
