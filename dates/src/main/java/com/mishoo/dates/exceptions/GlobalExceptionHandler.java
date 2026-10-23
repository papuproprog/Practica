package com.mishoo.dates.exceptions;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleHttpStatusCodeException(HttpStatusCodeException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString().isBlank()
                ? ex.getStatusText() : ex.getResponseBodyAsString());
    }

    @ExceptionHandler(DateNotFoundException.class)
    public ResponseEntity<String> handleDateNotFoundException(DateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BarberAlreadyBookedException.class)
    public ResponseEntity<String> handleBarberAlreadyBookedException(BarberAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(BarberTimeSlotOverlapException.class)
    public ResponseEntity<String> handleBarberTimeSlotOverlapException(BarberTimeSlotOverlapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ClientAlreadyBookedException.class)
    public ResponseEntity<String> handleClientAlreadyBookedException(ClientAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(OutsideBusinessHoursException.class)
    public ResponseEntity<String> handleOutsideBusinessHoursException(OutsideBusinessHoursException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientAdvanceNoticeException.class)
    public ResponseEntity<String> handleInsufficientAdvanceNoticeException(InsufficientAdvanceNoticeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DateTooFarInFutureException.class)
    public ResponseEntity<String> handleDateTooFarInFutureException(DateTooFarInFutureException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));

        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler({ResourceAccessException.class, CallNotPermittedException.class})
    public ResponseEntity<String> handleServiceUnavailable(Exception ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("External service (client-service or barber-service) is not available");
    }

}
