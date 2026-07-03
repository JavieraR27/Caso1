package com.example.administrador.dto;

/**
 * Resultado del login: JWT con rol ADMINISTRADOR + datos de la cuenta.
 */
public record LoginResponse(
        String token,
        AdministradorResponse administrador) {
}
