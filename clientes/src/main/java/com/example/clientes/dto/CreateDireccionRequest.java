package com.example.clientes.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Nueva dirección de despacho del cliente.
 */
public record CreateDireccionRequest(
        @NotBlank(message = "El alias no puede ser vacío")
        String alias,

        @NotBlank(message = "La calle no puede ser vacía")
        String calle,

        @NotBlank(message = "El número no puede ser vacío")
        String numero,

        @NotBlank(message = "La comuna no puede ser vacía")
        String comuna,

        @NotBlank(message = "La región no puede ser vacía")
        String region) {
}
