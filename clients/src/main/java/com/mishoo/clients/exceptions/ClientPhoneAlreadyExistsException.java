package com.mishoo.clients.exceptions;

public class ClientPhoneAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "A client with this phone number already exists";

    public ClientPhoneAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public ClientPhoneAlreadyExistsException(String message) {
        super(message);
    }

}
