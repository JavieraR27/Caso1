package com.example.proveedores.model;

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
 * Proveedor externo del marketplace (ej.: Ferretería Cóndor).
 * Postula con sus datos y documentos; el administrador aprueba o rechaza
 * la postulación dejando observaciones.
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoProveedor estado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDateTime fechaPostulacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public Proveedor() {}

    public Proveedor(int id, String rut, String razonSocial, String email, String telefono,
            EstadoProveedor estado, String observaciones, LocalDateTime fechaPostulacion,
            LocalDateTime fechaResolucion) {
        this.id = id;
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaPostulacion = fechaPostulacion;
        this.fechaResolucion = fechaResolucion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public EstadoProveedor getEstado() { return estado; }
    public void setEstado(EstadoProveedor estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDateTime fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
