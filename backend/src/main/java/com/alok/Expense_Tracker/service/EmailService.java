package com.alok.Expense_Tracker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service

public interface EmailService {
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendWelcomeEmail(String toEmail, String name);
    void sendPasswordResetEmail(String toEmail, String name, String token);
    void sendEmailVerificationEmail(String toEmail, String name, String token);
    void sendPasswordChangedNotificationEmail(String toEmail, String name);
}

