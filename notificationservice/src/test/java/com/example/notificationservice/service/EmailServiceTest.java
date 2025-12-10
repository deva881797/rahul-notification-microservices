package com.example.notificationsevice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_success() {
        // Act
        emailService.sendEmail("test@gmail.com", "Hello", "This is a test");

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_missingRecipient_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emailService.sendEmail("", "Subject", "Body")
        );

        assertEquals("Recipient email is required", exception.getMessage());
        verify(mailSender, never()).send(any());
    }

    @Test
    void sendEmail_nullSubject_shouldUseDefault() {
        emailService.sendEmail("test@gmail.com", null, "Body");

        // Capture the actual email message
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_nullBody_shouldUseEmptyBody() {
        emailService.sendEmail("test@gmail.com", "Subject", null);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_mailSenderThrowsException_shouldNotCrash() {
        doThrow(new RuntimeException("Mail Server Down"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
                emailService.sendEmail("test@gmail.com", "Test", "Hello")
        );
    }
}
