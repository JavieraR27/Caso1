package com.example.tickets.mapper;

import com.example.tickets.dto.MensajeResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketMensaje;

/**
 * Convierte las entidades del servicio de tickets a sus DTOs de salida.
 */
public class TicketMapper {

    public static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getVentaId(),
                ticket.getClienteId(),
                ticket.getProveedorId(),
                ticket.getCategoria(),
                ticket.getAsunto(),
                ticket.getDescripcion(),
                ticket.getEstado(),
                ticket.getResolucion(),
                ticket.isReembolsoAutorizado(),
                ticket.getFechaApertura(),
                ticket.getFechaResolucion());
    }

    public static MensajeResponse toResponse(TicketMensaje mensaje) {
        return new MensajeResponse(
                mensaje.getId(),
                mensaje.getTicket().getId(),
                mensaje.getAutorRol(),
                mensaje.getMensaje(),
                mensaje.getFecha());
    }
}
