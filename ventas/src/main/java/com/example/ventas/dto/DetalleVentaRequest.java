package com.example.ventas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Línea del carrito: producto y cantidad.
 */
public record DetalleVentaRequest(
        @NotNull(message = "El id del producto no puede ser nulo")
        @Positive(message = "El id del producto debe ser positivo")
        Integer productoId,

        @NotNull(message = "La cantidad no puede ser nula")
        @Positive(message = "La cantidad debe ser positiva")
        Integer cantidad) {
}
