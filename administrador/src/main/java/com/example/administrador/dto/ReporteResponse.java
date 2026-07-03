package com.example.administrador.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte semanal persistido, con su desglose por categoría.
 */
public record ReporteResponse(
        int id,
        int adminId,
        LocalDate semanaInicio,
        LocalDate semanaFin,
        int totalVentas,
        int totalComision,
        LocalDateTime fechaGeneracion,
        List<ReporteCategoriaResponse> categorias) {

    public record ReporteCategoriaResponse(
            String categoria,
            int unidades,
            int montoTotal,
            int comisionTotal) {}
}
