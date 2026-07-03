package com.example.administrador.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.administrador.dto.AccionResponse;
import com.example.administrador.dto.AprobacionProveedorRequest;
import com.example.administrador.dto.GenerarReporteRequest;
import com.example.administrador.dto.ProveedorResponse;
import com.example.administrador.dto.ReporteResponse;
import com.example.administrador.mapper.AdministradorMapper;
import com.example.administrador.model.AccionAdmin;
import com.example.administrador.model.ReporteSemanal;
import com.example.administrador.service.AdministradorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Acciones exclusivas del rol Administrador: bandeja y resolución de
 * postulaciones, reporte semanal por categoría y auditoría.
 */
@Tag(name = "Administración",
        description = "Acciones exclusivas del rol Administrador: aprobación de proveedores, "
                + "reporte semanal de ventas por categoría y auditoría de acciones. "
                + "Todas requieren JWT con rol ADMINISTRADOR")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdministradorService administradorService;

    public AdminController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    /**
     * Bandeja de postulaciones pendientes (vía proveedores?estado=POSTULADO).
     */
    @Operation(summary = "Bandeja de postulaciones pendientes",
            description = "Consulta vía WebClient al servicio proveedores los que están en "
                    + "estado POSTULADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulaciones pendientes (puede ser vacío)"),
            @ApiResponse(responseCode = "503", description = "Servicio proveedores no disponible")})
    @GetMapping("/proveedores/pendientes")
    public ResponseEntity<List<ProveedorResponse>> proveedoresPendientes() {
        return ResponseEntity.ok(administradorService.proveedoresPendientes());
    }

    /**
     * Aprueba o rechaza (con observaciones) una postulación: PATCH a
     * proveedores + aviso por notificaciones + registro en la auditoría.
     */
    @Operation(summary = "Aprueba o rechaza una postulación",
            description = "Orquesta la decisión: PATCH al servicio proveedores (el rechazo "
                    + "exige observaciones), aviso al vendedor vía notificaciones y registro "
                    + "en la auditoría (acciones_admin).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Decisión aplicada y auditada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El proveedor o el administrador no existen"),
            @ApiResponse(responseCode = "409", description = "Postulación ya resuelta o rechazo sin observaciones"),
            @ApiResponse(responseCode = "503", description = "Servicio proveedores o notificaciones no disponible")})
    @PostMapping("/proveedores/{id}/aprobacion")
    public ResponseEntity<AccionResponse> resolverProveedor(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Decisión del administrador sobre la postulación",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"adminId\": 1, \"aprobado\": true, "
                                    + "\"observaciones\": \"Documentación en regla\"}")))
            @Valid @RequestBody AprobacionProveedorRequest request) {
        AccionAdmin accion = administradorService.resolverProveedor(
                id, request.adminId(), request.aprobado(), request.observaciones());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdministradorMapper.toResponse(accion));
    }

    /**
     * Genera y persiste el reporte semanal de ventas por categoría
     * (agrega desde ventas vía WebClient).
     */
    @Operation(summary = "Genera el reporte semanal de ventas por categoría",
            description = "Obtiene vía WebClient las ventas PAGADAS del rango desde el "
                    + "servicio ventas, agrega por el snapshot de categoría de cada línea "
                    + "(unidades, monto, comisión) y PERSISTE el reporte para su descarga.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte generado y persistido"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El administrador no existe"),
            @ApiResponse(responseCode = "409", description = "Rango de fechas inválido"),
            @ApiResponse(responseCode = "503", description = "Servicio ventas no disponible")})
    @PostMapping("/reportes/semanal")
    public ResponseEntity<ReporteResponse> generarReporte(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rango de fechas del reporte (la semana)",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"adminId\": 1, \"desde\": \"2026-06-29\", \"hasta\": \"2026-07-05\"}")))
            @Valid @RequestBody GenerarReporteRequest request) {
        ReporteSemanal reporte = administradorService.generarReporte(
                request.adminId(), request.desde(), request.hasta());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdministradorMapper.toResponse(reporte));
    }

    @Operation(summary = "Lista los reportes generados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reportes persistidos (puede ser vacío)")})
    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteResponse>> listarReportes() {
        List<ReporteResponse> reportes = administradorService.listarReportes().stream()
                .map(AdministradorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(reportes);
    }

    /**
     * "Descarga" del reporte (JSON persistido).
     */
    @Operation(summary = "Descarga un reporte por id",
            description = "Devuelve el reporte persistido con su desglose por categoría.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte con desglose por categoría"),
            @ApiResponse(responseCode = "404", description = "No existe reporte con ese id")})
    @GetMapping("/reportes/{id}")
    public ResponseEntity<ReporteResponse> obtenerReporte(@PathVariable int id) {
        return ResponseEntity.ok(
                AdministradorMapper.toResponse(administradorService.obtenerReporte(id)));
    }

    @Operation(summary = "Auditoría de acciones administrativas",
            description = "Historial de aprobaciones, rechazos y reportes generados, "
                    + "ordenado del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acciones registradas (puede ser vacío)")})
    @GetMapping("/acciones")
    public ResponseEntity<List<AccionResponse>> listarAcciones() {
        List<AccionResponse> acciones = administradorService.listarAcciones().stream()
                .map(AdministradorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(acciones);
    }
}
