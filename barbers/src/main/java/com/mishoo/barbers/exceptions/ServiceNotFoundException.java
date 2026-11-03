package com.mishoo.barbers.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException() {
        super("El servicio solicitado no existe");
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }
}
