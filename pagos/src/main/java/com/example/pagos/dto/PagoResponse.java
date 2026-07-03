package com.example.pagos.dto;

import java.time.LocalDateTime;

import com.example.pagos.model.EstadoPago;
import com.example.pagos.model.MedioPago;

/**
 * Pago registrado.
 */
public record PagoResponse(
        int id,
        int ventaId,
        int clienteId,
        int monto,
        MedioPago medioPago,
        EstadoPago estado,
        LocalDateTime fechaPago) {
}
