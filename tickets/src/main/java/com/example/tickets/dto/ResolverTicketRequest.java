package com.example.tickets.dto;

import com.example.tickets.model.EstadoTicket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Resolución del administrador: RESUELTO o RECHAZADO, con constancia y,
 * si corresponde, autorización de reembolso (montoReembolso opcional:
 * si se omite, se reembolsa el monto completo del pago).
 */
public record ResolverTicketRequest(
        @NotNull(message = "El estado no puede ser nulo")
        EstadoTicket estado,

        @NotBlank(message = "La resolución no puede ser vacía")
        String resolucion,

        @NotNull(message = "reembolsoAutorizado no puede ser nulo")
        Boolean reembolsoAutorizado,

        @Positive(message = "El monto del reembolso debe ser positivo")
        Integer montoReembolso) {
}
