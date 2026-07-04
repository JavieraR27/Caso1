package com.example.feedback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Información OpenAPI 3 del servicio (springdoc). Swagger UI en
 * /swagger-ui/index.html y JSON en /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Feedback — Marketplace Paris")
                        .description("Reseñas con compra verificada y promedio de calificación por producto.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Pegar el token devuelto por el login (sin el prefijo 'Bearer ').")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
