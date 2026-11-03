package com.mishoo.dates.adapters;

import com.mishoo.dates.clients.BarbersServiceClient;
import com.mishoo.dates.schemas.BarberResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BarberAdapter {

    private final BarbersServiceClient barbersServiceClient;

    @CircuitBreaker(name = "barberService", fallbackMethod = "barberFallback")
    public BarberResponse getById(Long id) {
        return barbersServiceClient.getById(id);
    }

    public BarberResponse barberFallback(Long id, Throwable ex) throws Throwable {
        log.error("Barber connection error for ID {}: {}", id, ex.getMessage());
        throw ex;
    }

}
