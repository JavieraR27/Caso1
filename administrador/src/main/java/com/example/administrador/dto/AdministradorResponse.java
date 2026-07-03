package com.example.administrador.dto;

/**
 * Datos del administrador (sin password).
 */
public record AdministradorResponse(
        int id,
        String username,
        String nombre,
        String email) {
}
