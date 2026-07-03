package com.example.ventas.dto;

/**
 * Respuesta del servicio clientes (GET /api/v1/clientes/{id}).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record ClienteResponse(
        int id,
        String email,
        String nombre) {
}
