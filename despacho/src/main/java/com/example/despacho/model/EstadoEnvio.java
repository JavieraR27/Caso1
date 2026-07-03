package com.example.despacho.model;

/**
 * Estados del envío. ENTREGADO y CANCELADO son terminales: un seguimiento
 * en esos estados no admite más cambios.
 */
public enum EstadoEnvio {
    PENDIENTE,
    PREPARACION,
    ENVIADO,
    EN_REPARTO,
    ENTREGADO,
    CANCELADO;

    public boolean esTerminal() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
