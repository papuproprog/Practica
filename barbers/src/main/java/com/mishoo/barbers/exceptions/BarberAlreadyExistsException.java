package com.mishoo.barbers.exceptions;

public class BarberAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "A barber with this name already exists";

    public BarberAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public BarberAlreadyExistsException(String message) {
        super(message);
    }

}
