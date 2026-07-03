package com.example.tickets.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClients hacia ventas (compra real), pagos (reembolso autorizado) y
 * notificaciones (aviso de resolución).
 */
@Configuration
public class WebClientConfig {

    private WebClient build(WebClient.Builder builder, String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("ventasWebClient")
    public WebClient ventasWebClient(WebClient.Builder builder,
            @Value("${ventas.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("pagosWebClient")
    public WebClient pagosWebClient(WebClient.Builder builder,
            @Value("${pagos.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("notificacionesWebClient")
    public WebClient notificacionesWebClient(WebClient.Builder builder,
            @Value("${notificaciones.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }
}
