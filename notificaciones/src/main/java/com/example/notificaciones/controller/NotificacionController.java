package com.example.notificaciones.controller;

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

import com.example.notificaciones.dto.CreateNotificacionRequest;
import com.example.notificaciones.dto.NotificacionResponse;
import com.example.notificaciones.mapper.NotificacionMapper;
import com.example.notificaciones.model.DestinatarioTipo;
import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.model.TipoNotificacion;
import com.example.notificaciones.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Notificaciones",
        description = "Avisos del marketplace Paris a clientes y proveedores: compra "
                + "confirmada, despacho, aprobación de proveedor y resolución de reclamos "
                + "(envío simulado en EP2/EP3)")
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Registra un aviso (lo invocan ventas, despacho, tickets y administrador
     * vía WebClient).
     */
    @Operation(summary = "Registra un aviso",
            description = "Lo invocan ventas, despacho, tickets y administrador vía WebClient. "
                    + "El envío es simulado: queda ENVIADA al crearse. Requiere rol INTERNO "
                    + "o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aviso registrado (ENVIADA)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)")})
    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Aviso a registrar; referenciaId apunta a la venta, ticket o proveedor según el tipo",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"destinatarioTipo\": \"CLIENTE\", \"destinatarioId\": 1, "
                                    + "\"tipo\": \"DESPACHO\", \"asunto\": \"Tu pedido va en camino\", "
                                    + "\"mensaje\": \"El pedido de tu compra #1 fue despachado.\", "
                                    + "\"referenciaId\": 1}")))
            @Valid @RequestBody CreateNotificacionRequest request) {
        Notificacion notificacion = notificacionService.crear(NotificacionMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificacionMapper.toResponse(notificacion));
    }

    /**
     * Bandeja del cliente/proveedor, con filtros opcionales combinables.
     */
    @Operation(summary = "Lista la bandeja de avisos",
            description = "Filtros combinables: destinatarioTipo + destinatarioId (la bandeja "
                    + "de un cliente o proveedor) y tipo de aviso. Requiere rol CLIENTE, "
                    + "PROVEEDOR o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avisos (puede ser vacío)")})
    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listar(
            @RequestParam(name = "destinatarioTipo", required = false) DestinatarioTipo destinatarioTipo,
            @RequestParam(name = "destinatarioId", required = false) Integer destinatarioId,
            @RequestParam(name = "tipo", required = false) TipoNotificacion tipo) {
        List<NotificacionResponse> notificaciones = notificacionService
                .listar(destinatarioTipo, destinatarioId, tipo).stream()
                .map(NotificacionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(notificaciones);
    }

    @Operation(summary = "Busca un aviso por id",
            description = "Requiere rol CLIENTE, PROVEEDOR o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aviso encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe aviso con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<NotificacionResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(
                NotificacionMapper.toResponse(notificacionService.obtenerPorId(id)));
    }
}
