package com.example.productos.mapper;

import com.example.productos.dto.CreateProductoRequest;
import com.example.productos.dto.OfertaResponse;
import com.example.productos.dto.ProductoResponse;
import com.example.productos.model.Oferta;
import com.example.productos.model.Producto;

/**
 * Convierte entre los DTOs y las entidades del catálogo. La categoría, el
 * estado y las fechas los resuelve el service; el precio vigente se calcula
 * en el service y se pasa ya resuelto.
 */
public class ProductoMapper {

    public static Producto toModel(CreateProductoRequest request) {
        Producto producto = new Producto();
        producto.setProveedorId(request.proveedorId());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        return producto;
    }

    public static ProductoResponse toResponse(Producto producto, int precioVigente) {
        return new ProductoResponse(
                producto.getId(),
                producto.getProveedorId(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                precioVigente,
                producto.getStock(),
                producto.getEstado(),
                producto.getFechaCreacion());
    }

    public static OfertaResponse toResponse(Oferta oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getProducto().getId(),
                oferta.getTipoOferta(),
                oferta.getValor(),
                oferta.getFechaInicio(),
                oferta.getFechaFin(),
                oferta.isActiva());
    }
}
