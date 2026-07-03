package com.example.clientes.dto;

/**
 * Resultado del login: JWT con rol CLIENTE + datos del perfil.
 */
public record LoginResponse(
        String token,
        ClienteResponse cliente) {
}
