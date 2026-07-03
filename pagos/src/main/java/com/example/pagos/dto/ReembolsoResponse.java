package com.example.pagos.dto;

import java.time.LocalDateTime;

import com.example.pagos.model.EstadoReembolso;

/**
 * Reembolso con la referencia plana al pago.
 */
public record ReembolsoResponse(
        int id,
        int pagoId,
        Integer ticketId,
        int monto,
        String motivo,
        EstadoReembolso estado,
        LocalDateTime fecha) {
}
