package com.example.administrador.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Reporte semanal de ventas por categoría. Se PERSISTE al generarse
 * ("descarga el reporte semanal"): agrega los snapshots de categoría de los
 * detalles de venta obtenidos de ventas vía WebClient.
 */
@Entity
@Table(name = "reportes_semanales")
public class ReporteSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

    @Column(name = "semana_inicio", nullable = false)
    private LocalDate semanaInicio;

    @Column(name = "semana_fin", nullable = false)
    private LocalDate semanaFin;

    @Column(name = "total_ventas", nullable = false)
    private int totalVentas;

    @Column(name = "total_comision", nullable = false)
    private int totalComision;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /** Unidireccional con FK en reporte_categorias; se persisten juntas. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reporte_id", nullable = false)
    private List<ReporteCategoria> categorias = new ArrayList<>();

    public ReporteSemanal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }

    public LocalDate getSemanaInicio() { return semanaInicio; }
    public void setSemanaInicio(LocalDate semanaInicio) { this.semanaInicio = semanaInicio; }

    public LocalDate getSemanaFin() { return semanaFin; }
    public void setSemanaFin(LocalDate semanaFin) { this.semanaFin = semanaFin; }

    public int getTotalVentas() { return totalVentas; }
    public void setTotalVentas(int totalVentas) { this.totalVentas = totalVentas; }

    public int getTotalComision() { return totalComision; }
    public void setTotalComision(int totalComision) { this.totalComision = totalComision; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public List<ReporteCategoria> getCategorias() { return categorias; }
    public void setCategorias(List<ReporteCategoria> categorias) { this.categorias = categorias; }
}
