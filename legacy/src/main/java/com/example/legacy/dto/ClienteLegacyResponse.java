package com.example.legacy.dto;

import java.time.LocalDate;

/**
 * Datos del cliente histórico que se exponen a otros servicios (sin password).
 */
public record ClienteLegacyResponse(
        int id,
        String rut,
        String email,
        String nombre,
        LocalDate fechaRegistro) {
}
