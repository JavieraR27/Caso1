package com.example.pagos.model;

/**
 * Estado del reembolso. En EP2 el procesamiento es simulado: queda
 * PROCESADO al registrarse (pasarela real en backlog).
 */
public enum EstadoReembolso {
    SOLICITADO,
    PROCESADO
}
