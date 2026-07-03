package com.example.pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Reembolso de un pago, autorizado por el administrador al resolver un
 * reclamo (ticketId es referencia blanda al servicio tickets).
 */
@Entity
@Table(name = "reembolsos")
public class Reembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "monto", nullable = false)
    private int monto;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoReembolso estado;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public Reembolso() {}

    public Reembolso(int id, Pago pago, Integer ticketId, int monto, String motivo,
            EstadoReembolso estado, LocalDateTime fecha) {
        this.id = id;
        this.pago = pago;
        this.ticketId = ticketId;
        this.monto = monto;
        this.motivo = motivo;
        this.estado = estado;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoReembolso getEstado() { return estado; }
    public void setEstado(EstadoReembolso estado) { this.estado = estado; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
