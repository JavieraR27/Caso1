package com.example.clientes.dto;

import java.time.LocalDateTime;

import com.example.clientes.model.TipoCliente;

/**
 * Datos del cliente que se exponen (sin password). Lo consume ventas.
 */
public record ClienteResponse(
        int id,
        String email,
        String nombre,
        String telefono,
        TipoCliente tipo,
        Integer legacyId,
        LocalDateTime fechaCreacion) {
}
