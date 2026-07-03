package com.example.pagos.dto;

import java.time.LocalDateTime;

/**
 * Comprobante con la referencia plana al pago.
 */
public record ComprobanteResponse(
        int id,
        int pagoId,
        String folio,
        LocalDateTime fechaEmision) {
}
