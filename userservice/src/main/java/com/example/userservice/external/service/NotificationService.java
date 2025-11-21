package com.example.userservice.external.service;

import com.example.userservice.dto.OtpSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationService {

    @PostMapping("/notification/otp")
    void sendOtp(@RequestBody OtpSendRequest request);

}
