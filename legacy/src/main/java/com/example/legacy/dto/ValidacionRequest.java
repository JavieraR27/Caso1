package com.example.legacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales a validar contra el sistema legacy.
 */
public record ValidacionRequest(
        @NotBlank(message = "El email no puede ser vacío")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La password no puede ser vacía")
        String password) {
}
