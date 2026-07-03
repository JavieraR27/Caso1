package com.example.tickets.dto;

import com.example.tickets.model.CategoriaTicket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Apertura de un reclamo sobre una venta; el cliente y el proveedor se
 * toman de la venta al validar la compra real.
 */
public record CreateTicketRequest(
        @NotNull(message = "El id de la venta no puede ser nulo")
        @Positive(message = "El id de la venta debe ser positivo")
        Integer ventaId,

        @NotNull(message = "La categoría no puede ser nula")
        CategoriaTicket categoria,

        @NotBlank(message = "El asunto no puede ser vacío")
        String asunto,

        @NotBlank(message = "La descripción no puede ser vacía")
        String descripcion) {
}
