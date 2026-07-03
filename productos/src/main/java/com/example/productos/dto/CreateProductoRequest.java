package com.example.productos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Publicación de un producto (solo por un proveedor APROBADO; se valida
 * contra el servicio proveedores vía WebClient).
 */
public record CreateProductoRequest(
        @NotNull(message = "El id del proveedor no puede ser nulo")
        @Positive(message = "El id del proveedor debe ser positivo")
        Integer proveedorId,

        @NotNull(message = "El id de la categoría no puede ser nulo")
        @Positive(message = "El id de la categoría debe ser positivo")
        Integer categoriaId,

        @NotBlank(message = "El nombre no puede ser vacío")
        String nombre,

        String descripcion,

        @NotNull(message = "El precio no puede ser nulo")
        @Positive(message = "El precio debe ser positivo (CLP)")
        Integer precio,

        @NotNull(message = "El stock no puede ser nulo")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock) {
}
