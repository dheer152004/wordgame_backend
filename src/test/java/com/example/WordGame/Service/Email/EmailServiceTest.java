package com.example.WordGame.Service.Email;

import com.example.WordGame.modules.roles.user.Entities.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    @Test
    void emailVerificationUsesStyledHtmlAndIncludesVerificationLink() {
        EmailSender emailSender = mock(EmailSender.class);
        EmailService emailService = new EmailService(emailSender);
        User user = new User();
        user.setUsername("wordgame-player");
        user.setEmail("player@example.com");
        String verificationUrl = "https://example.com/verify?token=abc123";

        boolean sent = emailService.sendEmailVerificationSynchronously(user, verificationUrl);

        assertTrue(sent);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(
                org.mockito.ArgumentMatchers.eq("player@example.com"),
                org.mockito.ArgumentMatchers.eq("Verify your WordGame email"),
                htmlCaptor.capture(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        String html = htmlCaptor.getValue();
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Verify your WordGame account"));
        assertTrue(html.contains("aria-label=\"Google logo placeholder\""));
        assertTrue(html.contains("Hi <strong style=\"color:#f8fafc;\">wordgame-player</strong>"));
        assertTrue(html.contains("<a href=\"" + verificationUrl + "\""));
        assertTrue(html.contains(verificationUrl));
        assertTrue(html.contains("width:100%;"));
    }
}
