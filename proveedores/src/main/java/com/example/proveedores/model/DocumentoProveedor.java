package com.example.proveedores.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Documento adjunto a la postulación de un proveedor (certificados, patente, etc.).
 * Relación unidireccional: Proveedor no mantiene la colección para evitar
 * recursión al serializar a JSON.
 */
@Entity
@Table(name = "documentos_proveedor")
public class DocumentoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    private String nombreArchivo;

    @Column(name = "url", nullable = false, length = 300)
    private String url;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    public DocumentoProveedor() {}

    public DocumentoProveedor(int id, Proveedor proveedor, String tipo, String nombreArchivo,
            String url, LocalDateTime fechaCarga) {
        this.id = id;
        this.proveedor = proveedor;
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.url = url;
        this.fechaCarga = fechaCarga;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
}
