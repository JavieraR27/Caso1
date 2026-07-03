package com.example.clientes.dto;

/**
 * Dirección con la referencia plana al cliente (evita serializar la entidad
 * completa, que incluye la password).
 */
public record DireccionResponse(
        int id,
        int clienteId,
        String alias,
        String calle,
        String numero,
        String comuna,
        String region) {
}
