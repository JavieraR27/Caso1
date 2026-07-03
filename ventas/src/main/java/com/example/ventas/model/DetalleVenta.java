package com.example.ventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Línea de la orden. productoId/proveedorId son referencias blandas;
 * nombreProducto, categoria y precioUnitario son SNAPSHOTS: congelan el dato
 * histórico de la transacción y permiten el reporte semanal por categoría
 * sin acoplar administrador a productos.
 */
@Entity
@Table(name = "detalles_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "producto_id", nullable = false)
    private int productoId;

    @Column(name = "proveedor_id", nullable = false)
    private int proveedorId;

    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombreProducto;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    @Column(name = "subtotal", nullable = false)
    private int subtotal;

    @Column(name = "comision", nullable = false)
    private int comision;

    public DetalleVenta() {}

    public DetalleVenta(int id, int productoId, int proveedorId, String nombreProducto,
            String categoria, int cantidad, int precioUnitario, int subtotal, int comision) {
        this.id = id;
        this.productoId = productoId;
        this.proveedorId = proveedorId;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.comision = comision;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(int precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public int getComision() { return comision; }
    public void setComision(int comision) { this.comision = comision; }
}
