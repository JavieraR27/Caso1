package com.example.notificaciones.dto;

import com.example.notificaciones.model.DestinatarioTipo;
import com.example.notificaciones.model.TipoNotificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Aviso a registrar (lo invocan ventas, despacho, tickets y administrador
 * vía WebClient). referenciaId es opcional: id de la venta, ticket o
 * proveedor según el tipo.
 */
public record CreateNotificacionRequest(
        @NotNull(message = "El tipo de destinatario no puede ser nulo")
        DestinatarioTipo destinatarioTipo,

        @NotNull(message = "El id del destinatario no puede ser nulo")
        @Positive(message = "El id del destinatario debe ser positivo")
        Integer destinatarioId,

        @NotNull(message = "El tipo de notificación no puede ser nulo")
        TipoNotificacion tipo,

        @NotBlank(message = "El asunto no puede ser vacío")
        String asunto,

        @NotBlank(message = "El mensaje no puede ser vacío")
        String mensaje,

        @Positive(message = "El id de referencia debe ser positivo")
        Integer referenciaId) {
}
