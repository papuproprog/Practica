package com.mishoo.barbers.exceptions;

public class BarberNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Barber not found";

    public BarberNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public BarberNotFoundException(String message) {
        super(message);
    }

}
