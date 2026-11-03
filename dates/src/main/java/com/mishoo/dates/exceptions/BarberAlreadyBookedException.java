package com.mishoo.dates.exceptions;

public class BarberAlreadyBookedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The barber already has an active appointment at this date and time";

    public BarberAlreadyBookedException() {
        super(DEFAULT_MESSAGE);
    }

    public BarberAlreadyBookedException(String message) {
        super(message);
    }

}
