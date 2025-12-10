package com.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;


    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (subject == null) {
            subject = "Default Subject";
        }
        if (body == null) {
            body = "";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("send"+subject);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
}
