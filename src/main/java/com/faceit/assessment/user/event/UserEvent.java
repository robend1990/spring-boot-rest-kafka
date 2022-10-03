package com.faceit.assessment.user.event;

import com.faceit.assessment.infrastructure.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserEvent implements DomainEvent {
    private final String id;
}
