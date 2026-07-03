package com.example.pagos.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.pagos.security.JwtUtil;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClients del servicio: bean nombrado por destino, baseUrl por
 * properties, timeouts configurables (en la nube los cold starts exigen
 * valores holgados) y token INTERNO (JWT) adjunto en cada llamada saliente.
 */
@Configuration
public class WebClientConfig {

    private final JwtUtil jwtUtil;
    private final int connectTimeoutMs;
    private final int responseTimeoutSegundos;

    public WebClientConfig(JwtUtil jwtUtil,
            @Value("${paris.webclient.connecttimeoutms:3000}") int connectTimeoutMs,
            @Value("${paris.webclient.responsetimeout:5}") int responseTimeoutSegundos) {
        this.jwtUtil = jwtUtil;
        this.connectTimeoutMs = connectTimeoutMs;
        this.responseTimeoutSegundos = responseTimeoutSegundos;
    }

    private WebClient build(WebClient.Builder builder, String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSegundos));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> next.exchange(ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtUtil.generarInterno("pagos"))
                        .build()))
                .build();
    }

    @Bean("ventasWebClient")
    public WebClient ventasWebClient(WebClient.Builder builder,
            @Value("${ventas.service.url}") String baseUrl) {
        return build(builder, baseUrl);
    }
}
