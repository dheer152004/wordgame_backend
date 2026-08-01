package com.example.WordGame.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;

import com.example.WordGame.modules.roles.user.Entities.User;

import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@wordgame.example}")
    private String from;

    @Value("${app.mail.provider:gmail}")
    private String mailProvider;

    @Value("${app.mail.host:}")
    private String host;

    @Value("${app.mail.port:587}")
    private Integer port;

    @Value("${app.mail.username:}")
    private String username;

    @Value("${app.mail.password:}")
    private String password;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody){
        if (mailSender == null) {
            logger.warn("Mail sender is not configured. Skipping email delivery to {}.", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@klugword.com", "KLUG");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.debug("Sent email to {} subject={} via {}", to, subject, mailProvider);
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException ex) {
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
