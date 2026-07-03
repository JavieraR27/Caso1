package com.example.administrador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API Administrador — Marketplace Paris")
                .description("Aprobación de proveedores, auditoría y reporte semanal por categoría.")
                .version("v1"));
    }
}
