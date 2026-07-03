package com.example.tickets.dto;

import com.example.tickets.model.AutorRol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Mensaje del hilo de mediación (cliente, proveedor o administrador).
 */
public record CreateMensajeRequest(
        @NotNull(message = "El rol del autor no puede ser nulo")
        AutorRol autorRol,

        @NotBlank(message = "El mensaje no puede ser vacío")
        String mensaje) {
}
