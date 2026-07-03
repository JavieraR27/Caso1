package com.example.ventas.mapper;

import com.example.ventas.dto.DetalleVentaResponse;
import com.example.ventas.dto.VentaResponse;
import com.example.ventas.model.DetalleVenta;
import com.example.ventas.model.Venta;

/**
 * Convierte las entidades de la orden a sus DTOs de salida.
 * La construcción de la Venta (validaciones cruzadas, snapshots y montos)
 * vive en el service, no en el mapper.
 */
public class VentaMapper {

    public static VentaResponse toResponse(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getClienteId(),
                venta.getFecha(),
                venta.getEstado(),
                venta.getMontoTotal(),
                venta.getComisionTotal(),
                venta.getDetalles().stream().map(VentaMapper::toResponse).toList());
    }

    public static DetalleVentaResponse toResponse(DetalleVenta detalle) {
        return new DetalleVentaResponse(
                detalle.getId(),
                detalle.getProductoId(),
                detalle.getProveedorId(),
                detalle.getNombreProducto(),
                detalle.getCategoria(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal(),
                detalle.getComision());
    }
}
