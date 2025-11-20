package com.example.notificationsevice.controller;

import com.example.notificationsevice.dto.OtpRequest;
import com.example.notificationsevice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/otp")
    public ResponseEntity<String> sendOtp(@RequestBody OtpRequest request) {
        String message = "Your OTP for " + request.getPurpose() + " is: " + request.getOtp();
        emailService.sendEmail(request.getEmail(), request.getOtp(), message);
        return ResponseEntity.ok("OTP sent successfully");
    }

}
