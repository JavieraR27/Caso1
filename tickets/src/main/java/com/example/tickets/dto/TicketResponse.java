package com.example.tickets.dto;

import java.time.LocalDateTime;

import com.example.tickets.model.CategoriaTicket;
import com.example.tickets.model.EstadoTicket;

/**
 * Reclamo con su estado y constancia de resolución.
 */
public record TicketResponse(
        int id,
        int ventaId,
        int clienteId,
        int proveedorId,
        CategoriaTicket categoria,
        String asunto,
        String descripcion,
        EstadoTicket estado,
        String resolucion,
        boolean reembolsoAutorizado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaResolucion) {
}
