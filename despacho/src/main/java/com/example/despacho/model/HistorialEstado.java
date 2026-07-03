package com.example.despacho.model;

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
 * Registro trazable de cada cambio de estado del envío. Relación
 * unidireccional: Seguimiento no mantiene la colección.
 */
@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "seguimiento_id", nullable = false)
    private Seguimiento seguimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoEnvio estado;

    @Column(name = "comentario", length = 300)
    private String comentario;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    public HistorialEstado() {}

    public HistorialEstado(int id, Seguimiento seguimiento, EstadoEnvio estado,
            String comentario, LocalDateTime fechaCambio) {
        this.id = id;
        this.seguimiento = seguimiento;
        this.estado = estado;
        this.comentario = comentario;
        this.fechaCambio = fechaCambio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Seguimiento getSeguimiento() { return seguimiento; }
    public void setSeguimiento(Seguimiento seguimiento) { this.seguimiento = seguimiento; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }
}
