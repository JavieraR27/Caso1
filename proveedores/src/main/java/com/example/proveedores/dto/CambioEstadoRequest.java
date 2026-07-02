package com.example.proveedores.dto;

import com.example.proveedores.model.EstadoProveedor;

import jakarta.validation.constraints.NotNull;

/**
 * Resolución de la postulación (APROBADO o RECHAZADO) con observaciones;
 * obligatorias cuando se rechaza (se valida en el service).
 */
public record CambioEstadoRequest(
        @NotNull(message = "El estado no puede ser nulo")
        EstadoProveedor estado,

        String observaciones) {
}
