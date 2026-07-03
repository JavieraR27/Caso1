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

import jakarta.validation.Valid;

/**
 * Acciones exclusivas del rol Administrador: bandeja y resolución de
 * postulaciones, reporte semanal por categoría y auditoría.
 */
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
    @GetMapping("/proveedores/pendientes")
    public ResponseEntity<List<ProveedorResponse>> proveedoresPendientes() {
        return ResponseEntity.ok(administradorService.proveedoresPendientes());
    }

    /**
     * Aprueba o rechaza (con observaciones) una postulación: PATCH a
     * proveedores + aviso por notificaciones + registro en la auditoría.
     */
    @PostMapping("/proveedores/{id}/aprobacion")
    public ResponseEntity<AccionResponse> resolverProveedor(@PathVariable int id,
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
    @PostMapping("/reportes/semanal")
    public ResponseEntity<ReporteResponse> generarReporte(
            @Valid @RequestBody GenerarReporteRequest request) {
        ReporteSemanal reporte = administradorService.generarReporte(
                request.adminId(), request.desde(), request.hasta());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdministradorMapper.toResponse(reporte));
    }

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
    @GetMapping("/reportes/{id}")
    public ResponseEntity<ReporteResponse> obtenerReporte(@PathVariable int id) {
        return ResponseEntity.ok(
                AdministradorMapper.toResponse(administradorService.obtenerReporte(id)));
    }

    @GetMapping("/acciones")
    public ResponseEntity<List<AccionResponse>> listarAcciones() {
        List<AccionResponse> acciones = administradorService.listarAcciones().stream()
                .map(AdministradorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(acciones);
    }
}
