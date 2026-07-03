package com.example.ventas.model;

/**
 * Ciclo de vida de la orden: CREADA (carrito confirmado) → PAGADA
 * (dispara pago, despacho y notificación) | ANULADA.
 */
public enum EstadoVenta {
    CREADA,
    PAGADA,
    ANULADA
}
