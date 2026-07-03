package com.example.ventas.dto;

/**
 * Respuesta del servicio productos (GET /api/v1/productos/{id}).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record ProductoResponse(
        int id,
        int proveedorId,
        String categoriaNombre,
        String nombre,
        int precioVigente,
        int stock,
        String estado) {
}
