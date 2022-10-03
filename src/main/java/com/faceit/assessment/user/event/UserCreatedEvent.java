package com.faceit.assessment.user.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class UserCreatedEvent extends UserEvent {
    private final String email;

    public UserCreatedEvent(String id, String email) {
        super(id);
        this.email = email;
    }
}
