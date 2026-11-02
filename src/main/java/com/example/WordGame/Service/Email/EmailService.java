package com.example.WordGame.Service.Email;

import com.example.WordGame.modules.roles.user.Entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final EmailSender emailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name:Nroq}")
    private String fromName;

    @Value("${app.mail.reply-to:mail@nroq.in}")
    private String replyTo;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {

        if (to == null || to.isBlank()) {
            logger.warn("Email recipient is empty");
            return;
        }

        try {
            emailSender.send(to, normalizeSubject(subject), htmlBody, from, fromName, replyTo);
            logger.info("Email successfully sent to {} with subject '{}'",
                to, normalizeSubject(subject));
        } catch (RuntimeException ex) {
            logger.error("Failed to send email to {}", to, ex);
        }
    }

    public boolean sendHtmlEmailSynchronously(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) {
            logger.warn("Email recipient is empty");
            return false;
        }

        try {
            String normalizedSubject = normalizeSubject(subject);
            emailSender.send(to, normalizedSubject, htmlBody, from, fromName, replyTo);
            logger.info("Email successfully sent to {} with subject '{}'", to, normalizedSubject);
            return true;
        } catch (RuntimeException ex) {
            logger.error("Synchronous email delivery failed to {} with subject '{}'", to, normalizeSubject(subject), ex);
            return false;
        }
    }

    private String normalizeSubject(String subject) {
        return subject == null || subject.isBlank() ? "Nroq notification" : subject.trim();
    }

    

    public void sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>Welcome to <strong>Nroq</strong> — we're glad you're here!</p>\n<p>Get started by playing your first game and inviting friends.</p>",
                user.getUsername() != null ? user.getUsername() : "player");
        sendHtmlEmail(user.getEmail(), "Welcome to Nroq", body);
    }



    public void sendOtpEmail(User user, String otp) {
        if (user == null || user.getEmail() == null) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>Your verification code is: <strong>%s</strong></p>\n<p>This code expires in 10 minutes.</p>",
                user.getUsername() != null ? user.getUsername() : "player", otp);
        sendHtmlEmail(user.getEmail(), "Your Nroq verification code", body);
    }



    public void sendEmailVerification(User user, String verificationUrl) {
        if (user == null || user.getEmail() == null || verificationUrl == null || verificationUrl.isBlank()) return;
        String body = buildEmailVerificationHtml(
                user.getUsername() != null ? user.getUsername() : "player",
                verificationUrl);
        sendHtmlEmail(user.getEmail(), "Verify your Nroq email", body);
    }

    public boolean sendEmailVerificationSynchronously(User user, String verificationUrl) {
        if (user == null || user.getEmail() == null || verificationUrl == null || verificationUrl.isBlank()) {
            return false;
        }
        String body = buildEmailVerificationHtml(
                user.getUsername() != null ? user.getUsername() : "player",
                verificationUrl);
        return sendHtmlEmailSynchronously(user.getEmail(), "Verify your Nroq email", body);
    }

    private String buildEmailVerificationHtml(String username, String verificationUrl) {
        return String.format("""       
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <meta name="color-scheme" content="light">
                                <meta name="supported-color-schemes" content="light">

                                <title>Verify your Nroq account</title>

                                <style>
                                    /* =========================
                                    GENERAL RESET
                                    ========================= */

                                    body {
                                        margin: 0;
                                        padding: 0;
                                        width: 100%;
                                        background-color: #f4f6fa;
                                        font-family: Arial, Helvetica, sans-serif;
                                        color: #1e293b;
                                        -webkit-text-size-adjust: 100%;
                                    }

                                    table {
                                        border-collapse: collapse;
                                        border-spacing: 0;
                                    }

                                    img {
                                        display: block;
                                        border: 0;
                                        outline: none;
                                        text-decoration: none;
                                    }

                                    /* =========================
                                    EMAIL CONTAINER
                                    ========================= */

                                    .email-container {
                                        width: 100%;
                                        max-width: 600px;
                                        background-color: #ffffff;
                                        border: 1px solid #e2e8f0;
                                        border-radius: 16px;
                                    }

                                    .email-card {
                                        padding: 44px 48px;
                                    }

                                    /* =========================
                                    LEFT TO RIGHT WIPE
                                    ========================= */

                                    @keyframes wipeLeftToRight {
                                        0% {
                                            clip-path: inset(0 100% 0 0);
                                            visibility: visible;
                                        }

                                        100% {
                                            clip-path: inset(0 0 0 0);
                                            visibility: visible;
                                        }
                                    }

                                    /* =========================
                                    LOGO
                                    ========================= */

                                    .logo-wrapper {
                                        width: 100%;
                                        margin: 0 auto 32px;
                                        text-align: center;
                                    }

                                    .nroq-logo {
                                        display: block;
                                        width: 160px;
                                        max-width: 100%;
                                        height: auto;
                                        margin: 0 auto;

                                        clip-path: inset(0 100% 0 0);
                                        visibility: hidden;

                                        animation: wipeLeftToRight 1.5s ease-out 0.3s forwards;
                                    }

                                    /* =========================
                                    HEADING
                                    ========================= */

                                    .heading-wrapper {
                                        margin-bottom: 28px;
                                    }

                                    .email-title {
                                        margin: 0;
                                        color: #0f172a;
                                        font-size: 28px;
                                        line-height: 1.3;
                                        text-align: center;

                                        clip-path: inset(0 100% 0 0);
                                        visibility: hidden;

                                        animation: wipeLeftToRight 0.9s ease-out 1.9s forwards;
                                    }

                                    /* =========================
                                    GREETING
                                    ========================= */

                                    .greeting {
                                        margin: 0 0 20px;
                                        color: #475569;
                                        font-size: 16px;
                                        line-height: 1.6;

                                        clip-path: inset(0 100% 0 0);
                                        visibility: hidden;

                                        animation: wipeLeftToRight 0.8s ease-out 2.9s forwards;
                                    }

                                    .greeting strong {
                                        color: #0f172a;
                                    }

                                    /* =========================
                                    WELCOME MESSAGE
                                    ========================= */

                                    .welcome-message {
                                        margin: 0 0 28px;
                                        color: #475569;
                                        font-size: 16px;
                                        line-height: 1.7;

                                        clip-path: inset(0 100% 0 0);
                                        visibility: hidden;

                                        animation: wipeLeftToRight 0.9s ease-out 3.8s forwards;
                                    }

                                    /* =========================
                                    VERIFY BUTTON
                                    ========================= */

                                    .verify-wrapper {
                                        margin: 0 auto 32px;
                                        text-align: center;

                                        opacity: 0;

                                        animation:
                                            buttonAppear 0.7s ease-out 4.8s forwards,
                                            buttonZoom 1.8s ease-in-out 5.5s infinite;
                                    }

                                    .verify-button {
                                        display: inline-block;
                                        padding: 16px 36px;

                                        background-color: #2563eb;
                                        border: 1px solid #2563eb;
                                        border-radius: 8px;

                                        color: #ffffff !important;
                                        font-size: 16px;
                                        font-weight: 700;
                                        text-align: center;
                                        text-decoration: none;
                                    }

                                    /* =========================
                                    BUTTON ANIMATIONS
                                    ========================= */

                                    @keyframes buttonAppear {
                                        from {
                                            opacity: 0;
                                        }

                                        to {
                                            opacity: 1;
                                        }
                                    }

                                    @keyframes buttonZoom {
                                        0% {
                                            transform: scale(1);
                                        }

                                        50% {
                                            transform: scale(1.07);
                                        }

                                        100% {
                                            transform: scale(1);
                                        }
                                    }

                                    /* =========================
                                    SECURITY MESSAGE
                                    ========================= */

                                    .security-message {
                                        margin: 0;
                                        color: #64748b;
                                        font-size: 13px;
                                        line-height: 1.6;
                                        text-align: center;

                                        clip-path: inset(0 100% 0 0);
                                        visibility: hidden;

                                        animation: wipeLeftToRight 0.9s ease-out 5.7s forwards;
                                    }

                                    /* =========================
                                    DIVIDER
                                    ========================= */

                                    .email-divider {
                                        height: 1px;
                                        border: 0;
                                        background-color: #e2e8f0;
                                        margin: 0 0 22px;
                                    }

                                    /* =========================
                                    FOOTER
                                    ========================= */

                                    .bottom-footer {
                                        margin: 20px 0 0;
                                        color: #64748b;
                                        font-size: 12px;
                                        line-height: 1.5;
                                        text-align: center;
                                    }

                                    /* =========================
                                    REDUCED MOTION
                                    ========================= */

                                    @media (prefers-reduced-motion: reduce) {
                                        .nroq-logo,
                                        .email-title,
                                        .greeting,
                                        .welcome-message,
                                        .security-message {
                                            animation: none !important;
                                            clip-path: none !important;
                                            visibility: visible !important;
                                        }

                                        .verify-wrapper {
                                            animation: none !important;
                                            opacity: 1 !important;
                                        }
                                    }

                                    /* =========================
                                    MOBILE RESPONSIVE
                                    ========================= */

                                    @media only screen and (max-width: 600px) {

                                        .outer-wrapper {
                                            padding: 20px 10px !important;
                                        }

                                        .email-container {
                                            width: 100% !important;
                                            max-width: 100% !important;
                                            border-radius: 12px !important;
                                        }

                                        .email-card {
                                            padding: 28px 20px !important;
                                        }

                                        .logo-wrapper {
                                            margin-bottom: 26px !important;
                                        }

                                        .nroq-logo {
                                            width: 140px !important;
                                        }

                                        .heading-wrapper {
                                            margin-bottom: 24px !important;
                                        }

                                        .email-title {
                                            font-size: 24px !important;
                                            line-height: 1.35 !important;
                                        }

                                        .greeting {
                                            font-size: 15px !important;
                                        }

                                        .welcome-message {
                                            font-size: 15px !important;
                                            line-height: 1.65 !important;
                                        }

                                        .verify-button {
                                            padding: 15px 24px !important;
                                            font-size: 15px !important;
                                        }

                                        .security-message {
                                            font-size: 12px !important;
                                        }

                                        .bottom-footer {
                                            font-size: 11px !important;
                                            padding: 0 10px !important;
                                        }
                                    }

                                    /* =========================
                                    EXTRA SMALL MOBILE
                                    ========================= */

                                    @media only screen and (max-width: 360px) {

                                        .email-card {
                                            padding: 24px 16px !important;
                                        }

                                        .email-title {
                                            font-size: 22px !important;
                                        }

                                        .verify-button {
                                            padding: 14px 18px !important;
                                            font-size: 14px !important;
                                        }
                                    }

                                </style>
                            </head>

                            <body style="
                                margin:0;
                                padding:0;
                                background-color:#f4f6fa;
                                font-family:Arial,Helvetica,sans-serif;
                            ">

                                <!-- EMAIL PREVIEW -->
                                <div style="
                                    display:none;
                                    max-height:0;
                                    overflow:hidden;
                                    opacity:0;
                                ">
                                    Confirm your email address to activate your Nroq account.
                                </div>

                                <!-- MAIN BACKGROUND -->
                                <table
                                    role="presentation"
                                    width="100%"
                                    cellspacing="0"
                                    cellpadding="0"
                                    border="0"
                                    style="
                                        width:100%;
                                        background-color:#f4f6fa;
                                    "
                                >
                                    <tr>
                                        <td
                                            align="center"
                                            class="outer-wrapper"
                                            style="padding:36px 12px;"
                                        >

                                            <!-- EMAIL CARD -->
                                            <table
                                                role="presentation"
                                                class="email-container"
                                                width="600"
                                                cellspacing="0"
                                                cellpadding="0"
                                                border="0"
                                                style="
                                                    width:100%;
                                                    max-width:600px;
                                                    background-color:#ffffff;
                                                    border:1px solid #e2e8f0;
                                                    border-radius:16px;
                                                "
                                            >
                                                <tr>
                                                    <td
                                                        class="email-card"
                                                        style="padding:44px 48px;"
                                                    >

                                                        <!-- 1. NROQ LOGO -->
                                                        <div class="logo-wrapper">
                                                            <img
                                                                src="https://nroq.in/assets/fullnamelogo-J2tAzCZ8.svg"
                                                                alt="Nroq"
                                                                width="160"
                                                                class="nroq-logo"
                                                                style="
                                                                    display:block;
                                                                    width:160px;
                                                                    max-width:100%;
                                                                    height:auto;
                                                                    margin:0 auto;
                                                                "
                                                            />
                                                        </div>

                                                        <!-- 2. HEADING -->
                                                        <div class="heading-wrapper">
                                                            <h1 class="email-title">
                                                                Verify your Nroq account
                                                            </h1>
                                                        </div>

                                                        <!-- 3. GREETING -->
                                                        <p class="greeting">
                                                            Hi <strong>%s</strong>,
                                                        </p>

                                                        <!-- 4. WELCOME MESSAGE -->
                                                        <p class="welcome-message">
                                                            Thanks for signing up with Nroq.
                                                            Please verify your email address to
                                                            activate your account.
                                                        </p>

                                                        <!-- 5. VERIFY BUTTON -->
                                                        <table
                                                            role="presentation"
                                                            class="verify-wrapper"
                                                            cellspacing="0"
                                                            cellpadding="0"
                                                            border="0"
                                                            align="center"
                                                            style="margin:0 auto 32px;"
                                                        >
                                                            <tr>
                                                                <td
                                                                    align="center"
                                                                    bgcolor="#2563eb"
                                                                    style="border-radius:8px;"
                                                                >
                                                                    <a
                                                                        href="%s"
                                                                        class="verify-button"
                                                                        style="
                                                                            display:inline-block;
                                                                            padding:16px 36px;
                                                                            background-color:#2563eb;
                                                                            border:1px solid #2563eb;
                                                                            border-radius:8px;
                                                                            color:#ffffff;
                                                                            font-size:16px;
                                                                            font-weight:700;
                                                                            text-decoration:none;
                                                                        "
                                                                    >
                                                                        Verify Email Address
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </table>

                                                        <!-- DIVIDER -->
                                                        <hr
                                                            class="email-divider"
                                                            style="
                                                                height:1px;
                                                                border:0;
                                                                background-color:#e2e8f0;
                                                                margin:0 0 22px;
                                                            "
                                                        >

                                                        <!-- 6. SECURITY MESSAGE -->
                                                        <p class="security-message">
                                                            If you did not create a Nroq account,
                                                            you can safely ignore this email.
                                                        </p>

                                                    </td>
                                                </tr>
                                            </table>

                                            <!-- BOTTOM FOOTER -->
                                            <p class="bottom-footer">
                                                This is an automated message from Nroq.
                                                Please do not reply.
                                            </p>

                                        </td>
                                    </tr>
                                </table>
                            </body>
                            </html>
                            """, username, verificationUrl, verificationUrl);
    }



    public void sendPasswordResetEmail(User user, String resetUrl) {
        if (user == null || user.getEmail() == null || resetUrl == null || resetUrl.isBlank()) return;
        String body = String.format(
                "<p>Hi %s,</p>\n<p>We received a request to reset your Nroq password.</p>\n<p>Click the link below to set a new password:</p>\n<p><a href=\"%s\">Reset my password</a></p>\n<p>If you did not request this, you can safely ignore this email.</p>",
                user.getUsername() != null ? user.getUsername() : "player",
                resetUrl);
        sendHtmlEmail(user.getEmail(), "Reset your Nroq password", body);
    }



    public void sendNewItemNotification(String itemName, List<String> recipientEmails) {
        if (recipientEmails == null || recipientEmails.isEmpty()) return;
        String subject = "New item added: " + itemName;
        String body = String.format("<p>A new item <strong>%s</strong> was added to Nroq.</p>", itemName);
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
