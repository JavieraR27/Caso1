package com.example.administrador.model;

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


@Entity
@Table(name = "acciones_admin")
public class AccionAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoAccion tipo;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public AccionAdmin() {}

    public AccionAdmin(int id, Administrador administrador, TipoAccion tipo,
            Integer referenciaId, String observaciones, LocalDateTime fecha) {
        this.id = id;
        this.administrador = administrador;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.observaciones = observaciones;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }

    public TipoAccion getTipo() { return tipo; }
    public void setTipo(TipoAccion tipo) { this.tipo = tipo; }

    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
