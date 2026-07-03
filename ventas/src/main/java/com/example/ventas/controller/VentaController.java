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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Ventas",
        description = "Órdenes de compra del marketplace Paris: creación del carrito confirmado, "
                + "orquestación del pago (stock, pagos, despacho, notificaciones) y consultas")
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
    @Operation(summary = "Crea una orden de venta",
            description = "Valida el cliente (WebClient a clientes) y cada producto vía WebClient a "
                    + "productos (existencia, estado ACTIVO y stock); toma el precio vigente del "
                    + "catálogo como snapshot y calcula subtotales y comisión en el servidor. "
                    + "Requiere token de rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada en estado CREADA"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El cliente o algún producto no existe"),
            @ApiResponse(responseCode = "409", description = "Producto no ACTIVO o stock insuficiente"),
            @ApiResponse(responseCode = "503", description = "Servicio clientes o productos no disponible")})
    @PostMapping
    public ResponseEntity<VentaResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Carrito confirmado: cliente y líneas de productos (sin precios)",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"clienteId\": 1, \"detalles\": ["
                                    + "{\"productoId\": 1, \"cantidad\": 2}, "
                                    + "{\"productoId\": 3, \"cantidad\": 1}]}")))
            @Valid @RequestBody CreateVentaRequest request) {
        Venta venta = ventaService.crear(request.clienteId(), request.detalles());
        return ResponseEntity.status(HttpStatus.CREATED).body(VentaMapper.toResponse(venta));
    }

    /**
     * Orquestación de la live demo: descuenta stock, marca PAGADA y coordina
     * pagos, despacho y notificaciones.
     */
    @Operation(summary = "Paga una venta (orquestación)",
            description = "ORQUESTACIÓN del marketplace vía WebClient: descuenta stock en productos "
                    + "(si falla, la venta sigue CREADA), marca la venta PAGADA y luego coordina "
                    + "pagos (registra el pago y emite comprobante), despacho (crea el seguimiento) "
                    + "y notificaciones (aviso al cliente). Requiere token de rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta PAGADA y orquestación completada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La venta o algún producto no existe"),
            @ApiResponse(responseCode = "409", description = "La venta no está CREADA, stock insuficiente "
                    + "o un servicio orquestado rechazó la operación"),
            @ApiResponse(responseCode = "503", description = "Servicio productos, pagos, despacho "
                    + "o notificaciones no disponible")})
    @PatchMapping("{id}/pagar")
    public ResponseEntity<VentaResponse> pagar(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Medio de pago y dirección de entrega (snapshot para despacho)",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"medioPago\": \"TARJETA\", "
                                    + "\"direccionEntrega\": \"Av. Providencia 1234, Depto 502, Providencia, Santiago\"}")))
            @Valid @RequestBody PagarVentaRequest request) {
        Venta venta = ventaService.pagar(id, request.medioPago(), request.direccionEntrega());
        return ResponseEntity.ok(VentaMapper.toResponse(venta));
    }

    @Operation(summary = "Busca una venta por id",
            description = "Detalle de la orden con sus montos y comisión. La consumen también pagos y "
                    + "despacho para validación cruzada. Requiere token de rol CLIENTE, PROVEEDOR, "
                    + "ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe venta con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<VentaResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(VentaMapper.toResponse(ventaService.obtenerPorId(id)));
    }

    @Operation(summary = "Lista ventas con filtros opcionales",
            description = "Filtros excluyentes: clienteId (historial del cliente), proveedorId "
                    + "(órdenes del vendedor) o rango desde/hasta (insumo del reporte semanal). "
                    + "Sin filtros devuelve todas. Requiere token de rol CLIENTE, PROVEEDOR, "
                    + "ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de ventas (puede ser vacío)")})
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

    @Operation(summary = "Lista las líneas de una venta",
            description = "Detalle por producto con snapshot de precio, subtotal y comisión por línea. "
                    + "Requiere token de rol CLIENTE, PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Líneas de la venta"),
            @ApiResponse(responseCode = "404", description = "No existe venta con ese id")})
    @GetMapping("{id}/detalles")
    public ResponseEntity<List<DetalleVentaResponse>> listarDetalles(@PathVariable int id) {
        List<DetalleVentaResponse> detalles = ventaService.obtenerPorId(id).getDetalles().stream()
                .map(VentaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(detalles);
    }
}
