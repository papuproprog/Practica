package com.mishoo.dates.exceptions;

public class OutsideBusinessHoursException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Appointments cannot be scheduled during early morning or night hours. Business hours are from 08:00 to 20:00";

    public OutsideBusinessHoursException() {
        super(DEFAULT_MESSAGE);
    }

    public OutsideBusinessHoursException(String message) {
        super(message);
    }

}
