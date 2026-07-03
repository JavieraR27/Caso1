package com.example.notificaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Información OpenAPI 3 del servicio (springdoc). Swagger UI en
 * /swagger-ui/index.html y JSON en /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API Notificaciones — Marketplace Paris")
                .description("Registro y consulta de avisos a clientes y proveedores.")
                .version("v1"));
    }
}
