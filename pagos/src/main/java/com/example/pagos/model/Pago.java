package com.example.pagos.model;

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
 * Pago de una venta (un pago por venta). ventaId y clienteId son referencias
 * blandas: el monto y el cliente se toman de ventas al validar por WebClient.
 */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "venta_id", nullable = false, unique = true)
    private int ventaId;

    @Column(name = "cliente_id", nullable = false)
    private int clienteId;

    @Column(name = "monto", nullable = false)
    private int monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 15)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoPago estado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    public Pago() {}

    public Pago(int id, int ventaId, int clienteId, int monto, MedioPago medioPago,
            EstadoPago estado, LocalDateTime fechaPago) {
        this.id = id;
        this.ventaId = ventaId;
        this.clienteId = clienteId;
        this.monto = monto;
        this.medioPago = medioPago;
        this.estado = estado;
        this.fechaPago = fechaPago;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }

    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}
