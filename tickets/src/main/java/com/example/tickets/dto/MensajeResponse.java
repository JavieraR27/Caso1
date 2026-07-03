package com.example.tickets.dto;

import java.time.LocalDateTime;

import com.example.tickets.model.AutorRol;

/**
 * Mensaje del hilo con la referencia plana al ticket.
 */
public record MensajeResponse(
        int id,
        int ticketId,
        AutorRol autorRol,
        String mensaje,
        LocalDateTime fecha) {
}
