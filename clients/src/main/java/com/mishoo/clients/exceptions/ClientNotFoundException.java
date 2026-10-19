package com.mishoo.clients.exceptions;

public class ClientNotFoundException extends RuntimeException { // 👈 Tip: Usa RuntimeException

    private static final String DEFAULT_MESSAGE = "Client not found";

    public ClientNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public ClientNotFoundException(String message) {
        super(message);
    }


}