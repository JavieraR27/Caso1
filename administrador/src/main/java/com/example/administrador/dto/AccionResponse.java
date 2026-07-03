package com.example.administrador.dto;

import java.time.LocalDateTime;

import com.example.administrador.model.TipoAccion;

/**
 * Entrada de la auditoría de acciones administrativas.
 */
public record AccionResponse(
        int id,
        int adminId,
        TipoAccion tipo,
        Integer referenciaId,
        String observaciones,
        LocalDateTime fecha) {
}
