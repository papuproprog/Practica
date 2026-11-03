package com.mishoo.dates.clients;

import com.mishoo.dates.schemas.BarberResponse;
import com.mishoo.dates.schemas.ServiceResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("")
public interface BarbersServiceClient {

    @GetExchange("/api/barbers/{id}")
    BarberResponse getById(@PathVariable Long id);

    @GetExchange("/api/services/{id}")
    ServiceResponse getServiceById(@PathVariable Long id);

}
