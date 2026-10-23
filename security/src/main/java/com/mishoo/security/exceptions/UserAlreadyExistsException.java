package com.mishoo.security.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User with this email already exists";

    public UserAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
