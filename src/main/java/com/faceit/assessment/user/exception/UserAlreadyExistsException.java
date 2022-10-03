package com.faceit.assessment.user.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User with e-mail: " + email + " already exists");
    }
}
