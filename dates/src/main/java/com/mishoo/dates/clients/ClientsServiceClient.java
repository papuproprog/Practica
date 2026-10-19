package com.mishoo.dates.clients;
import com.mishoo.dates.schemas.ClientResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/clients")
public interface ClientsServiceClient {

    @GetExchange("/{id}")
    ClientResponse getById(@PathVariable Long id);

}
