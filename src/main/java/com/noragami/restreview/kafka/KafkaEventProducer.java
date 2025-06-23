package com.noragami.restreview.kafka;

import com.noragami.restreview.events.ResetPasswordEvent;
import com.noragami.restreview.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    // private final KafkaTemplate<String, ResetPasswordEvent> kafkaTemplateResetPassword;

    @Value("${kafka.topic.user-registered}")
    private String topic;

    @Value("${kafka.topic.reset-password}")
    private String passwordResetTopic;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send(topic, event);
    }

    public void sendResetPasswordEvent(ResetPasswordEvent event) {
        kafkaTemplate.send(passwordResetTopic, event);
    }

}
