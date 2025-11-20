package com.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisTemplate<String, String> redisTemplate;

    public String generateOtp(String email) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        // store OTP for 5 minutes
        redisTemplate.opsForValue().set("OTP_" + email, otp, 5, TimeUnit.MINUTES);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP_" + email);

        if(storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete("OTP_" + email);
            return true;
        }
        return false;
    }

}
