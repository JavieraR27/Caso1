package com.example.notificaciones.mapper;

import com.example.notificaciones.dto.CreateNotificacionRequest;
import com.example.notificaciones.dto.NotificacionResponse;
import com.example.notificaciones.model.Notificacion;

/**
 * Convierte entre los DTOs y la entidad Notificacion.
 * El estado y las fechas los define el service, no el mapper.
 */
public class NotificacionMapper {

    public static Notificacion toModel(CreateNotificacionRequest request) {
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatarioTipo(request.destinatarioTipo());
        notificacion.setDestinatarioId(request.destinatarioId());
        notificacion.setTipo(request.tipo());
        notificacion.setAsunto(request.asunto());
        notificacion.setMensaje(request.mensaje());
        notificacion.setReferenciaId(request.referenciaId());
        return notificacion;
    }

    public static NotificacionResponse toResponse(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getDestinatarioTipo(),
                notificacion.getDestinatarioId(),
                notificacion.getTipo(),
                notificacion.getAsunto(),
                notificacion.getMensaje(),
                notificacion.getEstado(),
                notificacion.getReferenciaId(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaEnvio());
    }
}
