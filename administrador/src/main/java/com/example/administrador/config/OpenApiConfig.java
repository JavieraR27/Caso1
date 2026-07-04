package com.example.administrador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Administrador — Marketplace Paris")
                        .description("Aprobación de proveedores, auditoría y reporte semanal por categoría.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Pegar el token devuelto por el login (sin el prefijo 'Bearer ').")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
