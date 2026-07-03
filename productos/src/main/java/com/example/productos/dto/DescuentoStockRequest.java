package com.example.productos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Descuento de stock al concretarse una venta (lo invoca el servicio ventas).
 */
public record DescuentoStockRequest(
        @NotNull(message = "La cantidad no puede ser nula")
        @Positive(message = "La cantidad a descontar debe ser positiva")
        Integer cantidad) {
}
