package com.example.tickets.model;

/**
 * Ciclo del reclamo: ABIERTO → EN_MEDIACION (cuando el administrador
 * interviene en el hilo) → RESUELTO | RECHAZADO (terminales).
 */
public enum EstadoTicket {
    ABIERTO,
    EN_MEDIACION,
    RESUELTO,
    RECHAZADO;

    public boolean esCerrado() {
        return this == RESUELTO || this == RECHAZADO;
    }
}
