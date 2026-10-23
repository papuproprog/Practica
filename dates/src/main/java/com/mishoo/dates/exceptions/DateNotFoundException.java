package com.mishoo.dates.exceptions;

public class DateNotFoundException extends RuntimeException {

    private final static String DEFAULT_MESSAGE = "Date not found";


    public DateNotFoundException(String message) {
        super(message);
    }


    public DateNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
