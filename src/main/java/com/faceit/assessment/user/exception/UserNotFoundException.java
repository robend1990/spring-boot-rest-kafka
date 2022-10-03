package com.faceit.assessment.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("No user with id " + userId.toString() + " found");
    }
}
