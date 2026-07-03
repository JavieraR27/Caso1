package com.example.ventas.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * El "carrito" confirmado: cliente y líneas de productos. Los precios NO
 * vienen del cliente; el servidor los toma del catálogo (precio vigente).
 */
public record CreateVentaRequest(
        @NotNull(message = "El id del cliente no puede ser nulo")
        @Positive(message = "El id del cliente debe ser positivo")
        Integer clienteId,

        @NotEmpty(message = "La venta debe tener al menos una línea")
        List<@Valid DetalleVentaRequest> detalles) {
}
