package com.example.userservice.service;

import com.example.userservice.dto.OtpSendRequest;
import com.example.userservice.external.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationCallerTest {

    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NotificationCaller notificationCaller;

    @Test
    void sendOtp_success() {
        // Arrange
        OtpSendRequest req = new OtpSendRequest();
        req.setOtp("234543");
        req.setEmail("example@gmail.com");
        req.setPurpose("REGISTER");

        // Act
        String result = notificationCaller.sendOtp(req);

        // Assert
        assertEquals("OTP sent successfully.", result);
        verify(notificationService, times(1)).sendOtp(req);
    }

    @Test
    void sendOtp_failure_shouldUseFallbackManually() {
        OtpSendRequest req = new OtpSendRequest();
        req.setOtp("234543");
        req.setEmail("example@gmail.com");
        req.setPurpose("REGISTER");

        doThrow(new RuntimeException("SMS API Down"))
                .when(notificationService)
                .sendOtp(req);

        // Since annotations won't trigger fallback, we call it ourselves after failure
        try {
            notificationCaller.sendOtp(req);
            fail("Exception expected");
        } catch (Exception ignored) {}

        String fallback = notificationCaller.fallback(req, new RuntimeException());

        assertEquals("Notification service is down. OTP sending failed.", fallback);
    }


    @Test
    void rateLimitFallback_shouldReturnCorrectMessage() {
        OtpSendRequest req = new OtpSendRequest();
        req.setOtp("234543");
        req.setEmail("example@gmail.com");
        req.setPurpose("REGISTER");

        String result = notificationCaller.rateLimitFallback(req, new RuntimeException());

        assertEquals("Too many requests. Please try again later.", result);
    }
}
