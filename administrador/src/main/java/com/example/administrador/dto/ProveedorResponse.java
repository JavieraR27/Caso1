package com.example.administrador.dto;

/**
 * Respuesta del servicio proveedores. DTO local del consumidor: solo los
 * campos que este servicio usa (bandeja de pendientes y decisión).
 */
public record ProveedorResponse(
        int id,
        String rut,
        String razonSocial,
        String email,
        String estado) {
}
