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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Envíos",
        description = "Seguimiento del despacho del marketplace Paris: uno por venta pagada, "
                + "con historial trazable de estados y aviso al cliente al despachar")
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
    @Operation(summary = "Crea el seguimiento de una venta",
            description = "Lo invoca ventas en la orquestación del pago. Validación cruzada "
                    + "WebClient: la venta debe existir y estar PAGADA; el cliente y el "
                    + "proveedor se toman de ella. Requiere rol INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seguimiento creado (PENDIENTE) con número ENV-"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La venta no existe en el servicio ventas"),
            @ApiResponse(responseCode = "409", description = "Ya hay seguimiento para esa venta o no está PAGADA"),
            @ApiResponse(responseCode = "503", description = "Servicio ventas no disponible")})
    @PostMapping
    public ResponseEntity<SeguimientoResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Venta pagada y dirección de entrega (snapshot)",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"ventaId\": 1, \"direccionEntrega\": \"Av. Providencia 123, Santiago\"}")))
            @Valid @RequestBody CreateEnvioRequest request) {
        Seguimiento seguimiento = seguimientoService.crear(
                request.ventaId(), request.direccionEntrega());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SeguimientoMapper.toResponse(seguimiento));
    }

    @Operation(summary = "Busca un seguimiento por id",
            description = "Requiere rol CLIENTE, PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe seguimiento con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<SeguimientoResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(
                SeguimientoMapper.toResponse(seguimientoService.obtenerPorId(id)));
    }

    /**
     * Consulta del cliente ("sigue el despacho desde su perfil") y del vendedor.
     */
    @Operation(summary = "Lista seguimientos",
            description = "Filtros opcionales: por venta, por cliente (el perfil de María) o "
                    + "por proveedor (órdenes por despachar del vendedor). Requiere rol "
                    + "CLIENTE, PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado (puede ser vacío)")})
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
    @Operation(summary = "Cambia el estado del envío (marcar enviado)",
            description = "Acción del vendedor: PREPARACION → ENVIADO → EN_REPARTO → ENTREGADO. "
                    + "Sin cambios desde estados terminales; al pasar a ENVIADO se avisa al "
                    + "cliente vía notificaciones (WebClient). Requiere rol PROVEEDOR o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado y registrado en el historial"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe seguimiento con ese id"),
            @ApiResponse(responseCode = "409", description = "Estado terminal o repetido"),
            @ApiResponse(responseCode = "503", description = "Servicio notificaciones no disponible")})
    @PatchMapping("{id}/estado")
    public ResponseEntity<SeguimientoResponse> cambiarEstado(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado del envío",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"estado\": \"ENVIADO\", \"comentario\": \"Retirado por courier\"}")))
            @Valid @RequestBody CambioEstadoEnvioRequest request) {
        Seguimiento seguimiento = seguimientoService.cambiarEstado(
                id, request.estado(), request.comentario());
        return ResponseEntity.ok(SeguimientoMapper.toResponse(seguimiento));
    }

    @Operation(summary = "Historial de estados del envío",
            description = "Trazabilidad completa con fecha y comentario de cada cambio. "
                    + "Requiere rol CLIENTE, PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial ordenado por fecha"),
            @ApiResponse(responseCode = "404", description = "No existe seguimiento con ese id")})
    @GetMapping("{id}/historial")
    public ResponseEntity<List<HistorialEstadoResponse>> historial(@PathVariable int id) {
        List<HistorialEstadoResponse> historial = seguimientoService.historial(id).stream()
                .map(SeguimientoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(historial);
    }
}
