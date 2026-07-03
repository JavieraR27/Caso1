package com.example.administrador.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Desglose del reporte semanal: totales por categoría de producto.
 */
@Entity
@Table(name = "reporte_categorias")
public class ReporteCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "unidades", nullable = false)
    private int unidades;

    @Column(name = "monto_total", nullable = false)
    private int montoTotal;

    @Column(name = "comision_total", nullable = false)
    private int comisionTotal;

    public ReporteCategoria() {}

    public ReporteCategoria(int id, String categoria, int unidades, int montoTotal,
            int comisionTotal) {
        this.id = id;
        this.categoria = categoria;
        this.unidades = unidades;
        this.montoTotal = montoTotal;
        this.comisionTotal = comisionTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }

    public int getMontoTotal() { return montoTotal; }
    public void setMontoTotal(int montoTotal) { this.montoTotal = montoTotal; }

    public int getComisionTotal() { return comisionTotal; }
    public void setComisionTotal(int comisionTotal) { this.comisionTotal = comisionTotal; }
}
