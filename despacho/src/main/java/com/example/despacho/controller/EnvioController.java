package com.example.despacho.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.despacho.dto.CambioEstadoEnvioRequest;
import com.example.despacho.dto.CreateEnvioRequest;
import com.example.despacho.dto.HistorialEstadoResponse;
import com.example.despacho.dto.SeguimientoResponse;
import com.example.despacho.mapper.SeguimientoMapper;
import com.example.despacho.model.Seguimiento;
import com.example.despacho.service.SeguimientoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/envios")
public class EnvioController {

    private final SeguimientoService seguimientoService;

    public EnvioController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    /**
     * Crea el seguimiento (lo invoca ventas); valida que la venta esté PAGADA.
     */
    @PostMapping
    public ResponseEntity<SeguimientoResponse> crear(
            @Valid @RequestBody CreateEnvioRequest request) {
        Seguimiento seguimiento = seguimientoService.crear(
                request.ventaId(), request.direccionEntrega());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SeguimientoMapper.toResponse(seguimiento));
    }

    @GetMapping("{id}")
    public ResponseEntity<SeguimientoResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(
                SeguimientoMapper.toResponse(seguimientoService.obtenerPorId(id)));
    }

    /**
     * Consulta del cliente ("sigue el despacho desde su perfil") y del vendedor.
     */
    @GetMapping
    public ResponseEntity<List<SeguimientoResponse>> listar(
            @RequestParam(name = "ventaId", required = false) Integer ventaId,
            @RequestParam(name = "clienteId", required = false) Integer clienteId,
            @RequestParam(name = "proveedorId", required = false) Integer proveedorId) {
        List<SeguimientoResponse> seguimientos = seguimientoService
                .listar(ventaId, clienteId, proveedorId).stream()
                .map(SeguimientoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(seguimientos);
    }

    /**
     * "Marcar enviado" del vendedor (y avances posteriores del reparto).
     */
    @PatchMapping("{id}/estado")
    public ResponseEntity<SeguimientoResponse> cambiarEstado(@PathVariable int id,
            @Valid @RequestBody CambioEstadoEnvioRequest request) {
        Seguimiento seguimiento = seguimientoService.cambiarEstado(
                id, request.estado(), request.comentario());
        return ResponseEntity.ok(SeguimientoMapper.toResponse(seguimiento));
    }

    @GetMapping("{id}/historial")
    public ResponseEntity<List<HistorialEstadoResponse>> historial(@PathVariable int id) {
        List<HistorialEstadoResponse> historial = seguimientoService.historial(id).stream()
                .map(SeguimientoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(historial);
    }
}
