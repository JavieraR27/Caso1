package com.example.feedback.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Reseña con compra verificada: solo reseña quien compró el producto (se
 * valida contra ventas). Una reseña por (cliente, producto, venta); todos
 * son referencias blandas a otros servicios.
 */
@Entity
@Table(name = "resenas", uniqueConstraints = @UniqueConstraint(
        name = "uk_resena_cliente_producto_venta",
        columnNames = {"cliente_id", "producto_id", "venta_id"}))
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "producto_id", nullable = false)
    private int productoId;

    @Column(name = "cliente_id", nullable = false)
    private int clienteId;

    @Column(name = "venta_id", nullable = false)
    private int ventaId;

    @Column(name = "calificacion", nullable = false)
    private int calificacion;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Resena() {}

    public Resena(int id, int productoId, int clienteId, int ventaId, int calificacion,
            String comentario, LocalDateTime fechaCreacion) {
        this.id = id;
        this.productoId = productoId;
        this.clienteId = clienteId;
        this.ventaId = ventaId;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
