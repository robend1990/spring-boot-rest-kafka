package com.faceit.assessment.user.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class UserRemovedEvent extends UserEvent {

    @JsonCreator
    public UserRemovedEvent(@JsonProperty("id") String id) {
        super(id);
    }
}
