package com.example.productos.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Alta de una categoría del marketplace.
 */
public record CreateCategoriaRequest(
        @NotBlank(message = "El nombre de la categoría no puede ser vacío")
        String nombre) {
}
