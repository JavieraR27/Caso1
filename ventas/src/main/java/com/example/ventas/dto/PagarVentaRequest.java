package com.example.ventas.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos para pagar la orden: el medio de pago lo valida el servicio pagos;
 * la dirección de entrega es el snapshot que usará despacho.
 */
public record PagarVentaRequest(
        @NotBlank(message = "El medio de pago no puede ser vacío")
        String medioPago,

        @NotBlank(message = "La dirección de entrega no puede ser vacía")
        String direccionEntrega) {
}
