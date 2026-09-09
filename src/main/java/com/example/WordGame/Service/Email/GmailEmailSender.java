package com.example.WordGame.Service.Email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "gmail", matchIfMissing = true)
public class GmailEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailSender.class);

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String htmlBody, String from) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            logger.error("Failed to send Gmail message to {}", to, ex);
            throw new IllegalStateException("Gmail email delivery failed", ex);
        }
    }
}