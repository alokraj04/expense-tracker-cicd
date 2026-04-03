package com.alok.Expense_Tracker.service.impl;

import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import com.alok.Expense_Tracker.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to: " + to, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("name", name);
        context.setVariable("loginUrl", frontendUrl + "/login");

        sendHtmlEmail(toEmail,
                "Welcome to " + appName + "!",
                buildFromTemplate("welcome", context));
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("name", name);
        context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + token);

        sendHtmlEmail(toEmail,
                "Reset your " + appName + " password",
                buildFromTemplate("password-reset", context));
    }

    @Override
    public void sendEmailVerificationEmail(String toEmail, String name, String token) {
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("name", name);
        context.setVariable("verifyUrl", frontendUrl + "/verify-email?token=" + token);

        sendHtmlEmail(toEmail,
                "Verify your new email — " + appName,
                buildFromTemplate("email-verification", context));
    }

    @Override
    public void sendPasswordChangedNotificationEmail(String toEmail, String name) {
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("name", name);
        context.setVariable("supportUrl", frontendUrl + "/support");

        sendHtmlEmail(toEmail,
                "Your password was changed — " + appName,
                buildFromTemplate("password-changed", context));
    }

    private String buildFromTemplate(String templateName, Context context) {
        return templateEngine.process("emails/" + templateName, context);
    }
}
