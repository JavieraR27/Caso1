package com.example.notificaciones.dto;

import java.time.LocalDateTime;

import com.example.notificaciones.model.DestinatarioTipo;
import com.example.notificaciones.model.EstadoNotificacion;
import com.example.notificaciones.model.TipoNotificacion;

/**
 * Notificación registrada, tal como la ve la bandeja del cliente/proveedor.
 */
public record NotificacionResponse(
        int id,
        DestinatarioTipo destinatarioTipo,
        int destinatarioId,
        TipoNotificacion tipo,
        String asunto,
        String mensaje,
        EstadoNotificacion estado,
        Integer referenciaId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEnvio) {
}
