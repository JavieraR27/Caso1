package com.example.administrador.dto;

import java.util.List;

public record VentaResponse(
        int id,
        String estado,
        int montoTotal,
        int comisionTotal,
        List<DetalleVentaRemoto> detalles) {


    public record DetalleVentaRemoto(
            String categoria,
            int cantidad,
            int subtotal,
            int comision) {}
}
