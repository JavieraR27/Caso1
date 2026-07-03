package com.example.productos.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient hacia el servicio proveedores (patrón del profe extendido: bean
 * nombrado, baseUrl por properties y timeouts de conexión/respuesta).
 */
@Configuration
public class WebClientConfig {

    @Bean("proveedoresWebClient")
    public WebClient proveedoresWebClient(WebClient.Builder builder,
            @Value("${proveedores.service.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
