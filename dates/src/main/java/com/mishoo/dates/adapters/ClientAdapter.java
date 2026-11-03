package com.mishoo.dates.adapters;

import com.mishoo.dates.clients.ClientsServiceClient;
import com.mishoo.dates.schemas.ClientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClientAdapter {

    private final ClientsServiceClient clientsServiceClient;

    @CircuitBreaker(name = "clientService", fallbackMethod = "clientFallback")
    public ClientResponse getById(Long id) {
        return clientsServiceClient.getById(id);
    }

    public ClientResponse clientFallback(Long id, Throwable ex) throws Throwable {
        log.error("Client connection error for ID {}: {}", id, ex.getMessage());
        throw ex;
    }

}
