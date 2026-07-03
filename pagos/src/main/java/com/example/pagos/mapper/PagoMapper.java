package com.example.pagos.mapper;

import com.example.pagos.dto.ComprobanteResponse;
import com.example.pagos.dto.PagoResponse;
import com.example.pagos.dto.ReembolsoResponse;
import com.example.pagos.model.Comprobante;
import com.example.pagos.model.Pago;
import com.example.pagos.model.Reembolso;

/**
 * Convierte las entidades del servicio de pagos a sus DTOs de salida.
 */
public class PagoMapper {

    public static PagoResponse toResponse(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getVentaId(),
                pago.getClienteId(),
                pago.getMonto(),
                pago.getMedioPago(),
                pago.getEstado(),
                pago.getFechaPago());
    }

    public static ComprobanteResponse toResponse(Comprobante comprobante) {
        return new ComprobanteResponse(
                comprobante.getId(),
                comprobante.getPago().getId(),
                comprobante.getFolio(),
                comprobante.getFechaEmision());
    }

    public static ReembolsoResponse toResponse(Reembolso reembolso) {
        return new ReembolsoResponse(
                reembolso.getId(),
                reembolso.getPago().getId(),
                reembolso.getTicketId(),
                reembolso.getMonto(),
                reembolso.getMotivo(),
                reembolso.getEstado(),
                reembolso.getFecha());
    }
}
