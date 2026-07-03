package com.example.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Registro de un cliente nuevo (Pedro).
 */
public record CreateClienteRequest(
        @NotBlank(message = "El email no puede ser vacío")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La password no puede ser vacía")
        String password,

        @NotBlank(message = "El nombre no puede ser vacío")
        String nombre,

        String telefono) {
}
