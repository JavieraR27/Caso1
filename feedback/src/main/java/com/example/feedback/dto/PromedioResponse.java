package com.example.feedback.dto;

/**
 * Reputación del producto: promedio de calificación y total de reseñas
 * (para "elegir entre ofertas de distintos vendedores").
 */
public record PromedioResponse(
        int productoId,
        double promedio,
        long total) {
}
