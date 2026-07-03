package com.example.despacho.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Seguimiento del envío de una venta (uno por venta). ventaId, clienteId y
 * proveedorId son referencias blandas tomadas de ventas al crear; la
 * dirección de entrega es un snapshot.
 */
@Entity
@Table(name = "seguimientos")
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "venta_id", nullable = false, unique = true)
    private int ventaId;

    @Column(name = "cliente_id", nullable = false)
    private int clienteId;

    @Column(name = "proveedor_id", nullable = false)
    private int proveedorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false, length = 12)
    private EstadoEnvio estadoActual;

    @Column(name = "numero_seguimiento", unique = true, length = 20)
    private String numeroSeguimiento;

    @Column(name = "direccion_entrega", nullable = false, length = 300)
    private String direccionEntrega;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Seguimiento() {}

    public Seguimiento(int id, int ventaId, int clienteId, int proveedorId,
            EstadoEnvio estadoActual, String numeroSeguimiento, String direccionEntrega,
            LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.ventaId = ventaId;
        this.clienteId = clienteId;
        this.proveedorId = proveedorId;
        this.estadoActual = estadoActual;
        this.numeroSeguimiento = numeroSeguimiento;
        this.direccionEntrega = direccionEntrega;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public EstadoEnvio getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoEnvio estadoActual) { this.estadoActual = estadoActual; }

    public String getNumeroSeguimiento() { return numeroSeguimiento; }
    public void setNumeroSeguimiento(String numeroSeguimiento) { this.numeroSeguimiento = numeroSeguimiento; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
