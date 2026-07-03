package com.example.despacho.dto;

import java.time.LocalDateTime;

import com.example.despacho.model.EstadoEnvio;

/**
 * Entrada del historial de estados (trazabilidad del envío).
 */
public record HistorialEstadoResponse(
        int id,
        int seguimientoId,
        EstadoEnvio estado,
        String comentario,
        LocalDateTime fechaCambio) {
}
