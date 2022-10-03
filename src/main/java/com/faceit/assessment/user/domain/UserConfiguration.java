package com.faceit.assessment.user.domain;

import com.faceit.assessment.infrastructure.DomainEventPublisher;
import com.faceit.assessment.user.event.UserEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
class UserConfiguration {

    UserFacade userFacade(DomainEventPublisher<UserEvent> domainEventPublisher) {
        UserCreator userCreator = new UserCreator(new BCryptPasswordEncoder());
        return new UserFacade(new InMemoryUserRepository(), userCreator, domainEventPublisher);
    }

    @Bean
    UserFacade userFacade(UserRepository userRepository, KafkaTemplate<String, UserEvent> kafkaTemplate) {
        UserCreator userCreator = new UserCreator(new BCryptPasswordEncoder());
        return new UserFacade(userRepository, userCreator, new UserEventPublisher(kafkaTemplate, "users"));
    }
}
