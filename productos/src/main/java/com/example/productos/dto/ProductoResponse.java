package com.example.productos.dto;

import java.time.LocalDateTime;

import com.example.productos.model.EstadoProducto;

/**
 * Producto del catálogo con su precio vigente (precio con la oferta activa
 * aplicada, si la hay). Lo consumen ventas y feedback.
 */
public record ProductoResponse(
        int id,
        int proveedorId,
        int categoriaId,
        String categoriaNombre,
        String nombre,
        String descripcion,
        int precio,
        int precioVigente,
        int stock,
        EstadoProducto estado,
        LocalDateTime fechaCreacion) {
}
