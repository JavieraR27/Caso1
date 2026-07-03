package com.example.despacho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Creación del seguimiento (lo invoca ventas en la orquestación del pago).
 */
public record CreateEnvioRequest(
        @NotNull(message = "El id de la venta no puede ser nulo")
        @Positive(message = "El id de la venta debe ser positivo")
        Integer ventaId,

        @NotBlank(message = "La dirección de entrega no puede ser vacía")
        String direccionEntrega) {
}
