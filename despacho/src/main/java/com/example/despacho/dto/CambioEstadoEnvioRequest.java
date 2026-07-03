package com.example.despacho.dto;

import com.example.despacho.model.EstadoEnvio;

import jakarta.validation.constraints.NotNull;

/**
 * Cambio de estado del envío ("marcar enviado" del vendedor y avances
 * posteriores del reparto).
 */
public record CambioEstadoEnvioRequest(
        @NotNull(message = "El estado no puede ser nulo")
        EstadoEnvio estado,

        String comentario) {
}
