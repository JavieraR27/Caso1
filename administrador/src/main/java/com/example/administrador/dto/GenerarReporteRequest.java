package com.example.administrador.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Generación del reporte semanal de ventas por categoría para un rango
 * de fechas.
 */
public record GenerarReporteRequest(
        @NotNull(message = "El id del administrador no puede ser nulo")
        @Positive(message = "El id del administrador debe ser positivo")
        Integer adminId,

        @NotNull(message = "La fecha de inicio no puede ser nula")
        LocalDate desde,

        @NotNull(message = "La fecha de fin no puede ser nula")
        LocalDate hasta) {
}
