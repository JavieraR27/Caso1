package com.example.administrador.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginAdminRequest(
        @NotBlank(message = "El username no puede ser vacío")
        String username,

        @NotBlank(message = "La password no puede ser vacía")
        String password) {
}
