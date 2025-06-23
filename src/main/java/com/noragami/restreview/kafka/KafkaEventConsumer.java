package com.noragami.restreview.kafka;

import com.noragami.restreview.events.ResetPasswordEvent;
import com.noragami.restreview.events.UserRegisteredEvent;
import com.noragami.restreview.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final EmailService emailService;

    @KafkaListener(topics = "${kafka.topic.user-registered}", groupId = "email-service")
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendWelcome(event.getEmail(), event.getName());
    }

    @KafkaListener(topics = "${kafka.topic.reset-password}", groupId = "email-service")
    public void handleResetPassword(ResetPasswordEvent event) {
        System.out.println(event);
        emailService.sendResetEmail(event.getEmail(), event.getOtp());
    }
}
