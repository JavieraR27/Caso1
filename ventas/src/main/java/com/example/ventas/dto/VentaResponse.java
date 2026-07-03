package com.example.ventas.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ventas.model.EstadoVenta;

/**
 * Orden con sus líneas. La consumen pagos, despacho, tickets, feedback y
 * administrador (cada uno con su DTO local de los campos que usa).
 */
public record VentaResponse(
        int id,
        int clienteId,
        LocalDateTime fecha,
        EstadoVenta estado,
        int montoTotal,
        int comisionTotal,
        List<DetalleVentaResponse> detalles) {
}
