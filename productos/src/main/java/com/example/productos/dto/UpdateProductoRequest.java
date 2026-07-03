package com.example.productos.dto;

import com.example.productos.model.EstadoProducto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Edición del producto por su vendedor (precio, stock, estado).
 */
public record UpdateProductoRequest(
        @NotBlank(message = "El nombre no puede ser vacío")
        String nombre,

        String descripcion,

        @NotNull(message = "El precio no puede ser nulo")
        @Positive(message = "El precio debe ser positivo (CLP)")
        Integer precio,

        @NotNull(message = "El stock no puede ser nulo")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El estado no puede ser nulo")
        EstadoProducto estado) {
}
