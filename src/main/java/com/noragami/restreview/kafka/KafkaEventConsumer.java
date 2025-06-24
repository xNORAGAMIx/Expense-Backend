package com.noragami.restreview.kafka;

import com.noragami.restreview.events.ResetPasswordEvent;
import com.noragami.restreview.events.UserRegisteredEvent;
import com.noragami.restreview.events.VerifyEmailEvent;
import com.noragami.restreview.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final EmailService emailService;
    private final ExecutorService emailExecutorService;

    @KafkaListener(topics = "${kafka.topic.user-registered}", groupId = "email-service")
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendWelcome(event.getEmail(), event.getName());
    }

    @KafkaListener(topics = "${kafka.topic.reset-password}", groupId = "email-service")
    public void handleResetPassword(ResetPasswordEvent event) {
        System.out.println(event);

        emailExecutorService.submit(() -> {
            try {
                emailService.sendResetEmail(event.getEmail(), event.getOtp());
            } catch (Exception e) {
                System.err.println("Email sending failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
        // emailService.sendResetEmail(event.getEmail(), event.getOtp());
    }

    @KafkaListener(topics = "${kafka.topic.verify-email}", groupId = "email-service")
    public void handleVerifyEmail(VerifyEmailEvent event) {
        System.out.println(event);

        emailExecutorService.submit(() -> {
            try {
                emailService.sendVerifyEmail(event.getEmail(), event.getOtp());
            } catch (Exception e) {
                System.err.println("Email sending failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
        // emailService.sendVerifyEmail(event.getEmail(), event.getOtp());
    }
}
