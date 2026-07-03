package com.example.pagos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pagos.dto.ComprobanteResponse;
import com.example.pagos.dto.CreatePagoRequest;
import com.example.pagos.dto.CreateReembolsoRequest;
import com.example.pagos.dto.PagoResponse;
import com.example.pagos.dto.ReembolsoResponse;
import com.example.pagos.mapper.PagoMapper;
import com.example.pagos.model.Pago;
import com.example.pagos.model.Reembolso;
import com.example.pagos.service.PagoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Pagos",
        description = "Pagos del marketplace Paris: registro del pago en la orquestación de ventas, "
                + "emisión de comprobantes y reembolsos autorizados vía tickets")
@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /**
     * Registra el pago (lo invoca ventas en la orquestación); valida contra
     * ventas que la venta exista y esté PAGADA, y emite el comprobante.
     */
    @PostMapping
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody CreatePagoRequest request) {
        Pago pago = pagoService.crear(request.ventaId(), request.medioPago());
        return ResponseEntity.status(HttpStatus.CREATED).body(PagoMapper.toResponse(pago));
    }

    @GetMapping("{id}")
    public ResponseEntity<PagoResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(PagoMapper.toResponse(pagoService.obtenerPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listar(
            @RequestParam(name = "ventaId", required = false) Integer ventaId) {
        List<PagoResponse> pagos = pagoService.listar(ventaId).stream()
                .map(PagoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("{id}/comprobante")
    public ResponseEntity<ComprobanteResponse> obtenerComprobante(@PathVariable int id) {
        return ResponseEntity.ok(PagoMapper.toResponse(pagoService.obtenerComprobante(id)));
    }

    /**
     * Registra el reembolso autorizado (lo invoca tickets al resolver).
     */
    @PostMapping("{id}/reembolsos")
    public ResponseEntity<ReembolsoResponse> crearReembolso(@PathVariable int id,
            @Valid @RequestBody CreateReembolsoRequest request) {
        Reembolso reembolso = pagoService.crearReembolso(
                id, request.ticketId(), request.monto(), request.motivo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PagoMapper.toResponse(reembolso));
    }

    @GetMapping("{id}/reembolsos")
    public ResponseEntity<List<ReembolsoResponse>> listarReembolsos(@PathVariable int id) {
        List<ReembolsoResponse> reembolsos = pagoService.listarReembolsos(id).stream()
                .map(PagoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(reembolsos);
    }
}
