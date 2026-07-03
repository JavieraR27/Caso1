package com.example.productos.dto;

import java.time.LocalDate;

import com.example.productos.model.TipoOferta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Nueva oferta para un producto (una sola activa por producto).
 */
public record CreateOfertaRequest(
        @NotNull(message = "El tipo de oferta no puede ser nulo")
        TipoOferta tipoOferta,

        @NotNull(message = "El valor no puede ser nulo")
        @Positive(message = "El valor de la oferta debe ser positivo")
        Integer valor,

        @NotNull(message = "La fecha de inicio no puede ser nula")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin no puede ser nula")
        LocalDate fechaFin) {
}
