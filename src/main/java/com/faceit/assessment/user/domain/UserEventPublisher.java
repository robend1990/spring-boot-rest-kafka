package com.faceit.assessment.user.domain;

import com.faceit.assessment.infrastructure.DomainEventPublisher;
import com.faceit.assessment.user.event.UserEvent;
import com.faceit.assessment.user.exception.DomainEventPublisherException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.TimeUnit;

@Slf4j
class UserEventPublisher implements DomainEventPublisher<UserEvent> {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public UserEventPublisher(KafkaTemplate<String, UserEvent> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishEvent(UserEvent event) {
        log.info("Sending notification to kafka...");
        try {
            SendResult<String, UserEvent> result = kafkaTemplate.send(topic, event.getId(), event).get(2, TimeUnit.SECONDS);
            log.info("Notification sent!");
        } catch (Exception e) {
            log.error("Sending notification to kafka failed!");
            throw new DomainEventPublisherException(e);
        }
    }
}
