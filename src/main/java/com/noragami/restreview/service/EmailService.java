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
        message.setSubject("Welcome to RestReview");
        message.setText("Hello, "+name+", \n Thanks for registering with us!\n Regards, development team.");

        mailSender.send(message);
    }

    // Send password reset OTP
    public void sendResetEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password reset OTP");
        message.setText("Your otp: , "+otp+", \n Expires in 10 mins!\n Regards, development team.");

        mailSender.send(message);
    }

    // Send otp for email verification
    public void sendVerifyEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Email verification OTP");
        message.setText("Your otp: , "+otp+", \n Expires in 24 hrs!\n Regards, development team.");

        mailSender.send(message);
    }
}
