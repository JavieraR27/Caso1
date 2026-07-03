package com.example.ventas.dto;

/**
 * Línea de la orden con sus snapshots (nombre, categoría, precio).
 */
public record DetalleVentaResponse(
        int id,
        int productoId,
        int proveedorId,
        String nombreProducto,
        String categoria,
        int cantidad,
        int precioUnitario,
        int subtotal,
        int comision) {
}
