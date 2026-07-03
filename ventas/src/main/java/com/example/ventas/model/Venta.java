package com.example.ventas.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Orden de compra. clienteId es referencia blanda (vive en el servicio
 * clientes y se valida por WebClient). Los montos y la comisión se calculan
 * SIEMPRE en el servidor, nunca se confían al cliente.
 */
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "cliente_id", nullable = false)
    private int clienteId;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoVenta estado;

    @Column(name = "monto_total", nullable = false)
    private int montoTotal;

    @Column(name = "comision_total", nullable = false)
    private int comisionTotal;

    /** Unidireccional con FK en detalles_venta; se persisten junto a la venta. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venta_id", nullable = false)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {}

    public Venta(int id, int clienteId, LocalDateTime fecha, EstadoVenta estado,
            int montoTotal, int comisionTotal, List<DetalleVenta> detalles) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.comisionTotal = comisionTotal;
        this.detalles = detalles;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public int getMontoTotal() { return montoTotal; }
    public void setMontoTotal(int montoTotal) { this.montoTotal = montoTotal; }

    public int getComisionTotal() { return comisionTotal; }
    public void setComisionTotal(int comisionTotal) { this.comisionTotal = comisionTotal; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
