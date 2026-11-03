package com.mishoo.dates.exceptions;

public class BarberTimeSlotOverlapException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The appointment overlaps with an existing appointment for this barber";

    public BarberTimeSlotOverlapException() {
        super(DEFAULT_MESSAGE);
    }

    public BarberTimeSlotOverlapException(String message) {
        super(message);
    }

}
