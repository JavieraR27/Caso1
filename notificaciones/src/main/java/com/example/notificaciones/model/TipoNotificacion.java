package com.example.notificaciones.model;

/**
 * Motivo de la notificación; determina a qué recurso apunta referenciaId
 * (venta, ticket o proveedor según el caso).
 */
public enum TipoNotificacion {
    DESPACHO,
    APROBACION_PROVEEDOR,
    RESOLUCION_RECLAMO,
    VENTA_CONFIRMADA
}
