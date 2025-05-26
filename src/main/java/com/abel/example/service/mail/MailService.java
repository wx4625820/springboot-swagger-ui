package com.abel.example.service.mail;

public interface MailService {
    void sendSimpleMail(String to, String subject, String text);
}
