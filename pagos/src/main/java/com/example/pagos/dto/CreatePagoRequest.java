package com.example.pagos.dto;

import com.example.pagos.model.MedioPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Registro del pago (lo invoca ventas en la orquestación). El monto y el
 * cliente NO vienen en el request: se toman de la venta al validarla.
 */
public record CreatePagoRequest(
        @NotNull(message = "El id de la venta no puede ser nulo")
        @Positive(message = "El id de la venta debe ser positivo")
        Integer ventaId,

        @NotNull(message = "El medio de pago no puede ser nulo")
        MedioPago medioPago) {
}
