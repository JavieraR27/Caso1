package com.example.ventas.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.ventas.security.JwtUtil;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClients del servicio: bean nombrado por destino, baseUrl por
 * properties, timeouts y token INTERNO (JWT) adjunto en cada llamada
 * saliente para el RBAC entre servicios.
 */
@Configuration
public class WebClientConfig {

    private final JwtUtil jwtUtil;

    public WebClientConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private WebClient build(WebClient.Builder builder, String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> next.exchange(ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtUtil.generarInterno("ventas"))
                        .build()))
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
