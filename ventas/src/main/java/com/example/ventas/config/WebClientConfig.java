package com.example.ventas.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClients del orquestador: un bean nombrado por servicio consumido
 * (clientes, productos, pagos, despacho, notificaciones), baseUrl por
 * properties y timeouts de conexión/respuesta.
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

    @Bean("clientesWebClient")
    public WebClient clientesWebClient(WebClient.Builder builder,
            @Value("${clientes.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("productosWebClient")
    public WebClient productosWebClient(WebClient.Builder builder,
            @Value("${productos.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("pagosWebClient")
    public WebClient pagosWebClient(WebClient.Builder builder,
            @Value("${pagos.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("despachoWebClient")
    public WebClient despachoWebClient(WebClient.Builder builder,
            @Value("${despacho.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }

    @Bean("notificacionesWebClient")
    public WebClient notificacionesWebClient(WebClient.Builder builder,
            @Value("${notificaciones.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }
}
