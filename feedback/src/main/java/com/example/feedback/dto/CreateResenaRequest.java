package com.example.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Nueva reseña. La compra verificada (el cliente compró ese producto en esa
 * venta) se valida contra ventas antes de guardar.
 */
public record CreateResenaRequest(
        @NotNull(message = "El id del producto no puede ser nulo")
        @Positive(message = "El id del producto debe ser positivo")
        Integer productoId,

        @NotNull(message = "El id del cliente no puede ser nulo")
        @Positive(message = "El id del cliente debe ser positivo")
        Integer clienteId,

        @NotNull(message = "El id de la venta no puede ser nulo")
        @Positive(message = "El id de la venta debe ser positivo")
        Integer ventaId,

        @NotNull(message = "La calificación no puede ser nula")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        Integer calificacion,

        String comentario) {
}
