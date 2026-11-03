package com.mishoo.dates.exceptions;

public class DateTooFarInFutureException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Appointments cannot be scheduled more than 60 days in advance";

    public DateTooFarInFutureException() {
        super(DEFAULT_MESSAGE);
    }

    public DateTooFarInFutureException(String message) {
        super(message);
    }

}
