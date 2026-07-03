package com.example.tickets.model;

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
 * Mensaje del hilo de mediación. Relación unidireccional: Ticket no
 * mantiene la colección.
 */
@Entity
@Table(name = "ticket_mensajes")
public class TicketMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "autor_rol", nullable = false, length = 15)
    private AutorRol autorRol;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public TicketMensaje() {}

    public TicketMensaje(int id, Ticket ticket, AutorRol autorRol, String mensaje,
            LocalDateTime fecha) {
        this.id = id;
        this.ticket = ticket;
        this.autorRol = autorRol;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public AutorRol getAutorRol() { return autorRol; }
    public void setAutorRol(AutorRol autorRol) { this.autorRol = autorRol; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
