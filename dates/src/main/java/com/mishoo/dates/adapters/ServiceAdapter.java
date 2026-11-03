package com.mishoo.dates.adapters;

import com.mishoo.dates.clients.BarbersServiceClient;
import com.mishoo.dates.schemas.ServiceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ServiceAdapter {

    private final BarbersServiceClient barbersServiceClient;

    @CircuitBreaker(name = "barberService", fallbackMethod = "serviceFallback")
    public ServiceResponse getById(Long id) {
        return barbersServiceClient.getServiceById(id);
    }

    public ServiceResponse serviceFallback(Long id, Throwable ex) throws Throwable {
        log.error("Service connection error for ID {}: {}", id, ex.getMessage());
        throw ex;
    }
}
