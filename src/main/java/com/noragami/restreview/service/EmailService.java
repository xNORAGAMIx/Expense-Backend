package com.noragami.restreview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    // Send email after registration
    public void sendWelcome(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Welcome to Udhaari.com 🎉");
        message.setText("Hey " + name + ",\n\n" +
                "Welcome aboard Udhaari.com – where settling bills is easier than splitting a pizza!\n\n" +
                "We’re thrilled to have you join our community of awesome folks who believe that money shouldn’t come between friends.\n\n" +
                "✅ Track your shared expenses.\n" +
                "✅ Split bills without the drama.\n" +
                "✅ Settle up and stay happy.\n\n" +
                "Go ahead and start creating groups, logging expenses, and putting an end to awkward 'Udhaari' conversations.\n\n" +
                "If you ever get stuck or just want to say hi, we’re just an email away. 🙂\n\n" +
                "Happy Settling!\n" +
                "— Team Udhaari.com 💸");

        mailSender.send(message);
    }

    // Send password reset OTP
    public void sendResetEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Reset Your Udhaari.com Password 🛡️");
        message.setText("Hello,\n\n" +
                "We received a request to reset your Udhaari.com account password.\n\n" +
                "🔐 Your One-Time Password (OTP): **" + otp + "**\n" +
                "⏳ This code will expire in 10 minutes for your security.\n\n" +
                "Didn't request this? No worries — just ignore this email and your account will remain secure.\n\n" +
                "Need help? Reach us at support@udhaari.com.\n\n" +
                "Stay secure,\n" +
                "— Team Udhaari.com 💸");

        mailSender.send(message);
    }

    // Send otp for email verification
    public void sendVerifyEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Verify Your Email - Udhaari.com ✅");
        message.setText("Hey there,\n\n" +
                "You're almost ready to join the Udhaari.com family! 🎉\n\n" +
                "🔐 Your Email Verification OTP is: **" + otp + "**\n" +
                "⏳ This code is valid for 24 hours.\n\n" +
                "Please enter this OTP on the website to verify your email and unlock all features.\n\n" +
                "If you didn't sign up on Udhaari.com, feel free to ignore this message.\n\n" +
                "Welcome aboard!\n" +
                "— Team Udhaari.com 💸");

        mailSender.send(message);
    }
}
