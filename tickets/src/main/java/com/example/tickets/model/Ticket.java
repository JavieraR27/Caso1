package com.example.tickets.model;

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
 * Reclamo sobre una compra real. ventaId, clienteId y proveedorId son
 * referencias blandas: se toman de ventas al validar la compra por WebClient.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "venta_id", nullable = false)
    private int ventaId;

    @Column(name = "cliente_id", nullable = false)
    private int clienteId;

    @Column(name = "proveedor_id", nullable = false)
    private int proveedorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 25)
    private CategoriaTicket categoria;

    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoTicket estado;

    @Column(name = "resolucion", length = 500)
    private String resolucion;

    @Column(name = "reembolso_autorizado", nullable = false)
    private boolean reembolsoAutorizado;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public Ticket() {}

    public Ticket(int id, int ventaId, int clienteId, int proveedorId,
            CategoriaTicket categoria, String asunto, String descripcion,
            EstadoTicket estado, String resolucion, boolean reembolsoAutorizado,
            LocalDateTime fechaApertura, LocalDateTime fechaResolucion) {
        this.id = id;
        this.ventaId = ventaId;
        this.clienteId = clienteId;
        this.proveedorId = proveedorId;
        this.categoria = categoria;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.resolucion = resolucion;
        this.reembolsoAutorizado = reembolsoAutorizado;
        this.fechaApertura = fechaApertura;
        this.fechaResolucion = fechaResolucion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public CategoriaTicket getCategoria() { return categoria; }
    public void setCategoria(CategoriaTicket categoria) { this.categoria = categoria; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public boolean isReembolsoAutorizado() { return reembolsoAutorizado; }
    public void setReembolsoAutorizado(boolean reembolsoAutorizado) { this.reembolsoAutorizado = reembolsoAutorizado; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
