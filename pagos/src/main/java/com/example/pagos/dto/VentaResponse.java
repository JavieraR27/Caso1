package com.example.pagos.dto;

/**
 * Respuesta del servicio ventas (GET /api/v1/ventas/{id}).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record VentaResponse(
        int id,
        int clienteId,
        String estado,
        int montoTotal) {
}
