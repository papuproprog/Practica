package com.mishoo.clients.exceptions;

public class ClientAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "A client with this name already exists";

    public ClientAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public ClientAlreadyExistsException(String message) {
        super(message);
    }

}
