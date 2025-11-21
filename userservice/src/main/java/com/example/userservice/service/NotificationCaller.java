package com.example.userservice.service;

import com.example.userservice.dto.OtpSendRequest;
import com.example.userservice.external.service.NotificationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationCaller {
    private final NotificationService notificationService;

    @RateLimiter(name = "notificationRateLimiter", fallbackMethod = "rateLimitFallback")
    @Retry(name = "notificationRetry",fallbackMethod = "fallback")
    @CircuitBreaker(name = "notificationBreaker", fallbackMethod = "fallback")
    public String sendOtp(OtpSendRequest req) {
        notificationService.sendOtp(req);  // exception caught HERE
        return "OTP sent successfully.";
    }

    public String fallback(OtpSendRequest req, Throwable ex) {
        return "Notification service is down. OTP sending failed.";
    }

    public String rateLimitFallback(OtpSendRequest req, Throwable ex) {
        return "Too many requests. Please try again later.";
    }
}
