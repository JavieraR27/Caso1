package com.example.ventas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.example.ventas.dto.CreateVentaRequest;
import com.example.ventas.dto.DetalleVentaResponse;
import com.example.ventas.dto.PagarVentaRequest;
import com.example.ventas.dto.VentaResponse;
import com.example.ventas.mapper.VentaMapper;
import com.example.ventas.model.Venta;
import com.example.ventas.service.VentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Crea la orden validando cliente y productos vía WebClient; los montos
     * y la comisión se calculan en el servidor.
     */
    @PostMapping
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody CreateVentaRequest request) {
        Venta venta = ventaService.crear(request.clienteId(), request.detalles());
        return ResponseEntity.status(HttpStatus.CREATED).body(VentaMapper.toResponse(venta));
    }

    /**
     * Orquestación de la live demo: descuenta stock, marca PAGADA y coordina
     * pagos, despacho y notificaciones.
     */
    @PatchMapping("{id}/pagar")
    public ResponseEntity<VentaResponse> pagar(@PathVariable int id,
            @Valid @RequestBody PagarVentaRequest request) {
        Venta venta = ventaService.pagar(id, request.medioPago(), request.direccionEntrega());
        return ResponseEntity.ok(VentaMapper.toResponse(venta));
    }

    @GetMapping("{id}")
    public ResponseEntity<VentaResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(VentaMapper.toResponse(ventaService.obtenerPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar(
            @RequestParam(name = "clienteId", required = false) Integer clienteId,
            @RequestParam(name = "proveedorId", required = false) Integer proveedorId,
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<VentaResponse> ventas = ventaService.listar(clienteId, proveedorId, desde, hasta)
                .stream()
                .map(VentaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("{id}/detalles")
    public ResponseEntity<List<DetalleVentaResponse>> listarDetalles(@PathVariable int id) {
        List<DetalleVentaResponse> detalles = ventaService.obtenerPorId(id).getDetalles().stream()
                .map(VentaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(detalles);
    }
}
