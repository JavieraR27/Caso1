package com.example.administrador.dto;

import java.util.List;

/**
 * Respuesta del servicio ventas (GET /api/v1/ventas?desde=&hasta=).
 * DTO local del consumidor: solo los campos que el reporte usa.
 */
public record VentaResponse(
        int id,
        String estado,
        int montoTotal,
        int comisionTotal,
        List<DetalleVentaRemoto> detalles) {

    /** Línea remota con los snapshots que agrega el reporte. */
    public record DetalleVentaRemoto(
            String categoria,
            int cantidad,
            int subtotal,
            int comision) {}
}
