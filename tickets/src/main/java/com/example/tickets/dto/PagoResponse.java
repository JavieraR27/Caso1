package com.example.tickets.dto;

/**
 * Respuesta del servicio pagos (GET /api/v1/pagos?ventaId=).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record PagoResponse(
        int id,
        int monto,
        String estado) {
}
