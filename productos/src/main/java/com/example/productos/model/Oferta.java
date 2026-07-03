package com.example.productos.model;

import java.time.LocalDate;

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
 * Oferta de un producto con vigencia. Una sola oferta activa por producto;
 * el precio vigente se calcula con la oferta activa cuya vigencia incluye hoy.
 */
@Entity
@Table(name = "ofertas")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_oferta", nullable = false, length = 15)
    private TipoOferta tipoOferta;

    @Column(name = "valor", nullable = false)
    private int valor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    public Oferta() {}

    public Oferta(int id, Producto producto, TipoOferta tipoOferta, int valor,
            LocalDate fechaInicio, LocalDate fechaFin, boolean activa) {
        this.id = id;
        this.producto = producto;
        this.tipoOferta = tipoOferta;
        this.valor = valor;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = activa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public TipoOferta getTipoOferta() { return tipoOferta; }
    public void setTipoOferta(TipoOferta tipoOferta) { this.tipoOferta = tipoOferta; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
