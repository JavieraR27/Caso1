package com.example.administrador.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Acceso del administrador (simple en EP2; JWT en backlog EA3).
 */
public record LoginAdminRequest(
        @NotBlank(message = "El username no puede ser vacío")
        String username,

        @NotBlank(message = "La password no puede ser vacía")
        String password) {
}
