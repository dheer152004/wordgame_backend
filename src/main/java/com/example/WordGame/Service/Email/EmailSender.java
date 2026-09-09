package com.example.WordGame.Service.Email;

public interface EmailSender {

    void send(String to, String subject, String htmlBody, String from);
}