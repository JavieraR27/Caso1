package com.example.proveedores.dto;

/**
 * Resultado del login: JWT con rol PROVEEDOR + datos del vendedor.
 */
public record LoginResponse(
        String token,
        ProveedorResponse proveedor) {
}
