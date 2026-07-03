package com.example.despacho.mapper;

import com.example.despacho.dto.HistorialEstadoResponse;
import com.example.despacho.dto.SeguimientoResponse;
import com.example.despacho.model.HistorialEstado;
import com.example.despacho.model.Seguimiento;

/**
 * Convierte las entidades del servicio de despacho a sus DTOs de salida.
 */
public class SeguimientoMapper {

    public static SeguimientoResponse toResponse(Seguimiento seguimiento) {
        return new SeguimientoResponse(
                seguimiento.getId(),
                seguimiento.getVentaId(),
                seguimiento.getClienteId(),
                seguimiento.getProveedorId(),
                seguimiento.getEstadoActual(),
                seguimiento.getNumeroSeguimiento(),
                seguimiento.getDireccionEntrega(),
                seguimiento.getFechaCreacion(),
                seguimiento.getFechaActualizacion());
    }

    public static HistorialEstadoResponse toResponse(HistorialEstado historial) {
        return new HistorialEstadoResponse(
                historial.getId(),
                historial.getSeguimiento().getId(),
                historial.getEstado(),
                historial.getComentario(),
                historial.getFechaCambio());
    }
}
