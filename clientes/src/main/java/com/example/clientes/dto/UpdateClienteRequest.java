package com.example.clientes.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Actualización del perfil (el email y la password no se editan en EP2).
 */
public record UpdateClienteRequest(
        @NotBlank(message = "El nombre no puede ser vacío")
        String nombre,

        String telefono) {
}
