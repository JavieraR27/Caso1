package com.example.despacho.dto;

import java.time.LocalDateTime;

import com.example.despacho.model.EstadoEnvio;

/**
 * Seguimiento del envío tal como lo ven el cliente y el vendedor.
 */
public record SeguimientoResponse(
        int id,
        int ventaId,
        int clienteId,
        int proveedorId,
        EstadoEnvio estadoActual,
        String numeroSeguimiento,
        String direccionEntrega,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {
}
