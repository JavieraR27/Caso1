package com.example.despacho.dto;

import java.util.List;

/**
 * Respuesta del servicio ventas (GET /api/v1/ventas/{id}).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record VentaResponse(
        int id,
        int clienteId,
        String estado,
        List<DetalleVentaRemoto> detalles) {

    /** Línea remota: solo interesa el proveedor. */
    public record DetalleVentaRemoto(int proveedorId) {}
}
