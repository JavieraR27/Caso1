package com.example.administrador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdministradorRequest(
        @NotBlank(message = "El username no puede ser vacío")
        String username,

        @NotBlank(message = "La password no puede ser vacía")
        String password,

        @NotBlank(message = "El nombre no puede ser vacío")
        String nombre,

        @NotBlank(message = "El email no puede ser vacío")
        @Email(message = "El email no tiene un formato válido")
        String email) {
}
