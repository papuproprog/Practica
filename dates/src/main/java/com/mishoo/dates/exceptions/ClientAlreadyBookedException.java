package com.mishoo.dates.exceptions;

public class ClientAlreadyBookedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The client already has an active appointment at this date and time";

    public ClientAlreadyBookedException() {
        super(DEFAULT_MESSAGE);
    }

    public ClientAlreadyBookedException(String message) {
        super(message);
    }

}
