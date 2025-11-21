package com.example.notificationsevice.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    public EmailServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendEmail_success() {
        // Arrange
        String to = "test@example.com";
        String subject = "OTP Verification";
        String text = "123456";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendEmail(to, subject, text);

        // Assert
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sentMsg = captor.getValue();

        assertEquals(to, sentMsg.getTo()[0]);
        assertEquals(subject, sentMsg.getSubject());
        assertEquals(text, sentMsg.getText());
    }

    @Test
    void sendEmail_exceptionHandled() {
        // Arrange
        String to = "test@example.com";
        String subject = "OTP Verification";
        String text = "123456";

        doThrow(new RuntimeException("Send failed"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        // Act + Assert
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, text));
    }
}
