package com.mishoo.dates.exceptions;

public class InsufficientAdvanceNoticeException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Appointments must be scheduled with at least 30 minutes in advance";

    public InsufficientAdvanceNoticeException() {
        super(DEFAULT_MESSAGE);
    }

    public InsufficientAdvanceNoticeException(String message) {
        super(message);
    }

}
