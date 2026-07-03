package com.example.pagos.model;

/**
 * Estado del pago: PAGADO al registrarse; REEMBOLSADO cuando tickets
 * autoriza la devolución; ANULADO queda para casos administrativos.
 */
public enum EstadoPago {
    PAGADO,
    REEMBOLSADO,
    ANULADO
}
