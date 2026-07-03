package com.example.productos.dto;

/**
 * Respuesta del servicio proveedores (GET /api/v1/proveedores/{id}).
 * DTO local del consumidor: solo los campos que este servicio usa.
 */
public record ProveedorResponse(
        int id,
        String razonSocial,
        String estado) {
}
