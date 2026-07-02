package com.example.legacy.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente del sistema legacy de Almacenes Paris (5.000 registros históricos).
 * La password se guarda en texto plano porque simula un sistema antiguo;
 * el cifrado con BCrypt queda en el backlog EA3.
 */
@Entity
@Table(name = "clientes_legacy")
public class ClienteLegacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    public ClienteLegacy() {}

    public ClienteLegacy(int id, String rut, String email, String password,
            String nombre, LocalDate fechaRegistro) {
        this.id = id;
        this.rut = rut;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
