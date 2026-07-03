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
    @Operation(summary = "Registra el pago de una venta",
            description = "Lo invoca ventas en la orquestación del pago. Validación cruzada "
                    + "WebClient: la venta debe existir en ventas y estar PAGADA; el monto y "
                    + "el cliente se toman de ella. Emite el comprobante automáticamente. "
                    + "Requiere rol INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado y comprobante emitido"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La venta no existe en el servicio ventas"),
            @ApiResponse(responseCode = "409", description = "Ya hay un pago para esa venta o la venta no está PAGADA"),
            @ApiResponse(responseCode = "503", description = "Servicio ventas no disponible")})
    @PostMapping
    public ResponseEntity<PagoResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Venta a pagar y medio de pago",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"ventaId\": 1, \"medioPago\": \"TARJETA\"}")))
            @Valid @RequestBody CreatePagoRequest request) {
        Pago pago = pagoService.crear(request.ventaId(), request.medioPago());
        return ResponseEntity.status(HttpStatus.CREATED).body(PagoMapper.toResponse(pago));
    }

    @Operation(summary = "Busca un pago por id",
            description = "Requiere rol CLIENTE, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe pago con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<PagoResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(PagoMapper.toResponse(pagoService.obtenerPorId(id)));
    }

    @Operation(summary = "Lista los pagos",
            description = "Filtro opcional por venta (?ventaId=). Lo usa tickets para ubicar "
                    + "el pago a reembolsar. Requiere rol CLIENTE, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado (puede ser vacío)")})
    @GetMapping
    public ResponseEntity<List<PagoResponse>> listar(
            @RequestParam(name = "ventaId", required = false) Integer ventaId) {
        List<PagoResponse> pagos = pagoService.listar(ventaId).stream()
                .map(PagoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pagos);
    }

    @Operation(summary = "Obtiene el comprobante del pago",
            description = "Respaldo formal de la transacción, con folio único. "
                    + "Requiere rol CLIENTE, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comprobante con su folio"),
            @ApiResponse(responseCode = "404", description = "No existe el pago o su comprobante")})
    @GetMapping("{id}/comprobante")
    public ResponseEntity<ComprobanteResponse> obtenerComprobante(@PathVariable int id) {
        return ResponseEntity.ok(PagoMapper.toResponse(pagoService.obtenerComprobante(id)));
    }

    /**
     * Registra el reembolso autorizado (lo invoca tickets al resolver).
     */
    @Operation(summary = "Registra un reembolso del pago",
            description = "Lo invoca tickets cuando el administrador autoriza la devolución al "
                    + "resolver un reclamo. Un reembolso por pago; el monto no puede exceder "
                    + "lo pagado; deja el pago REEMBOLSADO. Requiere rol INTERNO o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reembolso registrado (PROCESADO)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe pago con ese id"),
            @ApiResponse(responseCode = "409", description = "El pago no está PAGADO o el monto excede lo pagado")})
    @PostMapping("{id}/reembolsos")
    public ResponseEntity<ReembolsoResponse> crearReembolso(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reembolso autorizado por el administrador",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"ticketId\": 1, \"monto\": 40000, "
                                    + "\"motivo\": \"Reclamo #1: taladro defectuoso confirmado\"}")))
            @Valid @RequestBody CreateReembolsoRequest request) {
        Reembolso reembolso = pagoService.crearReembolso(
                id, request.ticketId(), request.monto(), request.motivo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PagoMapper.toResponse(reembolso));
    }

    @Operation(summary = "Lista los reembolsos del pago",
            description = "Requiere rol CLIENTE, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reembolsos del pago (puede ser vacío)"),
            @ApiResponse(responseCode = "404", description = "No existe pago con ese id")})
    @GetMapping("{id}/reembolsos")
    public ResponseEntity<List<ReembolsoResponse>> listarReembolsos(@PathVariable int id) {
        List<ReembolsoResponse> reembolsos = pagoService.listarReembolsos(id).stream()
                .map(PagoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(reembolsos);
    }
}
