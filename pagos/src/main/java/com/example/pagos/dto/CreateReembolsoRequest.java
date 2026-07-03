package com.example.pagos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Reembolso autorizado (lo invoca tickets al resolver un reclamo).
 */
public record CreateReembolsoRequest(
        @Positive(message = "El id del ticket debe ser positivo")
        Integer ticketId,

        @NotNull(message = "El monto no puede ser nulo")
        @Positive(message = "El monto debe ser positivo")
        Integer monto,

        @NotBlank(message = "El motivo no puede ser vacío")
        String motivo) {
}
