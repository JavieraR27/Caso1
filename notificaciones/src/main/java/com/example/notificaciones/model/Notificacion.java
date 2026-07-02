package com.example.notificaciones.model;

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
 * Aviso dirigido a un cliente o proveedor. El destinatario y la referencia son
 * referencias blandas (sin FK física) a entidades de otros servicios; el envío
 * de correo real queda en el backlog EA3, aquí se simula al crear.
 */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "destinatario_tipo", nullable = false)
    private DestinatarioTipo destinatarioTipo;

    @Column(name = "destinatario_id", nullable = false)
    private int destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacion tipo;

    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNotificacion estado;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    public Notificacion() {}

    public Notificacion(int id, DestinatarioTipo destinatarioTipo, int destinatarioId,
            TipoNotificacion tipo, String asunto, String mensaje, EstadoNotificacion estado,
            Integer referenciaId, LocalDateTime fechaCreacion, LocalDateTime fechaEnvio) {
        this.id = id;
        this.destinatarioTipo = destinatarioTipo;
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.estado = estado;
        this.referenciaId = referenciaId;
        this.fechaCreacion = fechaCreacion;
        this.fechaEnvio = fechaEnvio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public DestinatarioTipo getDestinatarioTipo() { return destinatarioTipo; }
    public void setDestinatarioTipo(DestinatarioTipo destinatarioTipo) { this.destinatarioTipo = destinatarioTipo; }

    public int getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(int destinatarioId) { this.destinatarioId = destinatarioId; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}
