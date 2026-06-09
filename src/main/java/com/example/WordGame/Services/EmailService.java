package com.example.WordGame.Services;

import com.example.WordGame.Entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@wordgame.example}")
    private String from;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.debug("Sent email to {} subject={}", to, subject);
        } catch (Exception ex) {
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
