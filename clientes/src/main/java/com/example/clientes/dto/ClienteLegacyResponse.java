package com.example.clientes.dto;

/**
 * Respuesta del servicio legacy (POST /api/v1/legacy/validaciones).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record ClienteLegacyResponse(
        int id,
        String email,
        String nombre) {
}
