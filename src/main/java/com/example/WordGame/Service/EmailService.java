package com.example.WordGame.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.WordGame.modules.roles.user.Entities.User;

import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class EmailService {

    private final SesClient sesClient;

    @Value("${app.mail.from:no-reply@wordgame.example}")
    private String from;

    @Value("${app.mail.provider:ses}")
    private String mailProvider;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody){
        if (!"ses".equalsIgnoreCase(mailProvider)) {
            logger.debug("Email provider '{}' is disabled for this profile", mailProvider);
            return;
        }
        if (sesClient == null) {
            logger.warn("SES client is not configured. Skipping email delivery to {}.", to);
            return;
        }

        try {
            Message message = Message.builder()
                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                    .body(Body.builder().html(Content.builder().data(htmlBody).charset("UTF-8").build()).build())
                    .build();
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(from)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(message)
                    .build());
            logger.debug("Sent email to {} subject={} via {}", to, subject, mailProvider);
        } catch (software.amazon.awssdk.services.ses.model.SesException ex) {
            logger.error("Failed to send email to {}", to, ex);
        }
    }

    public void sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>Welcome to <strong>WordGame</strong> — we're glad you're here!</p>\n<p>Get started by playing your first game and inviting friends.</p>",
                user.getUsername() != null ? user.getUsername() : "player");
        sendHtmlEmail(user.getEmail(), "Welcome to WordGame", body);
    }

    public void sendOtpEmail(User user, String otp) {
        if (user == null || user.getEmail() == null) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>Your verification code is: <strong>%s</strong></p>\n<p>This code expires in 10 minutes.</p>",
                user.getUsername() != null ? user.getUsername() : "player", otp);
        sendHtmlEmail(user.getEmail(), "Your WordGame verification code", body);
    }

    public void sendEmailVerification(User user, String verificationUrl) {
        if (user == null || user.getEmail() == null || verificationUrl == null || verificationUrl.isBlank()) return;
        String body = String.format(
                "<p>Hi <h4> %s </h4>,</p> \n<p>Thanks for signing up with WordGame.</p> \n<p>Please confirm your email address by clicking the link below:</p>\n<p><a href=\"%s\">Verify my email</a></p>\n<p>If you did not create this account, you can ignore this email.</p>",
                user.getUsername() != null ? user.getUsername() : "player",
                verificationUrl);
        sendHtmlEmail(user.getEmail(), "Verify your WordGame email", body);
    }
    public void sendPasswordResetEmail(User user, String resetUrl) {
        if (user == null || user.getEmail() == null || resetUrl == null || resetUrl.isBlank()) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>We received a request to reset your WordGame password.</p>\n<p>Click the link below to set a new password:</p>\n<p><a href=\"%s\">Reset my password</a></p>\n<p>If you did not request this, you can safely ignore this email.</p>",
                user.getUsername() != null ? user.getUsername() : "player",
                resetUrl);
        sendHtmlEmail(user.getEmail(), "Reset your WordGame password", body);
    }
    public void sendNewItemNotification(String itemName, List<String> recipientEmails) {
        if (recipientEmails == null || recipientEmails.isEmpty()) return;
        String subject = "New item added: " + itemName;
        String body = String.format("<p>A new item <strong>%s</strong> was added to WordGame.</p>", itemName);
        for (String to : recipientEmails) {
            if (to != null && !to.isBlank()) sendHtmlEmail(to, subject, body);
        }
    }

    public void sendNewItemNotification(String itemName, List<User> users, boolean useEmailsOnly) {
        List<String> emails = new ArrayList<>();
        if (users != null) {
            for (User u : users) {
                if (u != null && u.getEmail() != null) emails.add(u.getEmail());
            }
        }
        sendNewItemNotification(itemName, emails);
    }
}
