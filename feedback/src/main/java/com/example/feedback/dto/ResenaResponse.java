package com.example.feedback.dto;

import java.time.LocalDateTime;

/**
 * Reseña publicada.
 */
public record ResenaResponse(
        int id,
        int productoId,
        int clienteId,
        int ventaId,
        int calificacion,
        String comentario,
        LocalDateTime fechaCreacion) {
}
