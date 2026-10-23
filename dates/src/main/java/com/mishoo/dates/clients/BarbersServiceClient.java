package com.mishoo.dates.clients;

import com.mishoo.dates.schemas.BarberResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/barbers")
public interface BarbersServiceClient {

    @GetExchange("/{id}")
    BarberResponse getById(@PathVariable Long id);

}
