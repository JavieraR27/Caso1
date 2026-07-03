package com.example.productos.dto;

import java.time.LocalDate;

import com.example.productos.model.TipoOferta;

/**
 * Oferta con la referencia plana al producto.
 */
public record OfertaResponse(
        int id,
        int productoId,
        TipoOferta tipoOferta,
        int valor,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activa) {
}
