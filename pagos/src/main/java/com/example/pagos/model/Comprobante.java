package com.example.pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Comprobante del pago (1:1, folio único). Se emite automáticamente al
 * registrarse el pago: es el respaldo formal de la transacción.
 */
@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToOne
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;

    @Column(name = "folio", nullable = false, unique = true, length = 20)
    private String folio;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    public Comprobante() {}

    public Comprobante(int id, Pago pago, String folio, LocalDateTime fechaEmision) {
        this.id = id;
        this.pago = pago;
        this.folio = folio;
        this.fechaEmision = fechaEmision;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
}
