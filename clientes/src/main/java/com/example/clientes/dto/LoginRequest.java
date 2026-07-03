package com.example.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de login. Si el email no existe localmente se validan contra
 * el sistema legacy y, de ser correctas, el cliente histórico se migra.
 */
public record LoginRequest(
        @NotBlank(message = "El email no puede ser vacío")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La password no puede ser vacía")
        String password) {
}
