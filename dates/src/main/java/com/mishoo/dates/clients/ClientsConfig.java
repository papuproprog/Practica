package com.mishoo.dates.clients;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class ClientsConfig {

    @Bean
    public ClientsServiceClient clientsServiceClient(LoadBalancerClient loadBalancerClient) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                .baseUrl("http://client-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new HttpClientErrorException(res.getStatusCode(), res.getStatusText());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new HttpServerErrorException(res.getStatusCode(), "Client-service fallo interno");
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(ClientsServiceClient.class);
    }

}
