package com.example.tickets.controller;

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

import com.example.tickets.dto.CreateMensajeRequest;
import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.MensajeResponse;
import com.example.tickets.dto.ResolverTicketRequest;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.mapper.TicketMapper;
import com.example.tickets.model.EstadoTicket;
import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketMensaje;
import com.example.tickets.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tickets",
        description = "Reclamos postventa del marketplace Paris: apertura sobre compras reales, "
                + "hilo de mediación entre cliente, proveedor y administrador, y resolución "
                + "con reembolso opcional vía pagos")
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Abre un reclamo sobre una compra real (validada contra ventas).
     */
    @Operation(summary = "Abre un reclamo sobre una compra",
            description = "Validación cruzada WebClient: la venta debe existir en el servicio "
                    + "ventas y tener líneas reclamables; el cliente y el proveedor se toman "
                    + "de la venta. Solo puede llamarlo el rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reclamo abierto en estado ABIERTO"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La venta no existe en el servicio ventas"),
            @ApiResponse(responseCode = "409", description = "La venta no tiene líneas reclamables"),
            @ApiResponse(responseCode = "503", description = "Servicio ventas no disponible")})
    @PostMapping
    public ResponseEntity<TicketResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del reclamo sobre una venta real",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"ventaId\": 1, \"categoria\": \"PRODUCTO_DEFECTUOSO\", "
                                    + "\"asunto\": \"Taladro percutor llegó defectuoso\", "
                                    + "\"descripcion\": \"El taladro Bauker 650W no enciende; "
                                    + "pido cambio o reembolso.\"}")))
            @Valid @RequestBody CreateTicketRequest request) {
        Ticket datos = new Ticket();
        datos.setCategoria(request.categoria());
        datos.setAsunto(request.asunto());
        datos.setDescripcion(request.descripcion());

        Ticket ticket = ticketService.crear(request.ventaId(), datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketMapper.toResponse(ticket));
    }

    /**
     * Bandeja del administrador (?estado=ABIERTO) y reclamos del cliente.
     */
    @Operation(summary = "Lista reclamos con filtros opcionales",
            description = "Bandeja del administrador (?estado=ABIERTO) y reclamos de un "
                    + "cliente (?clienteId=). Pueden llamarlo los roles CLIENTE, PROVEEDOR "
                    + "y ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de reclamos (puede ser vacío)")})
    @GetMapping
    public ResponseEntity<List<TicketResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoTicket estado,
            @RequestParam(name = "clienteId", required = false) Integer clienteId) {
        List<TicketResponse> tickets = ticketService.listar(estado, clienteId).stream()
                .map(TicketMapper::toResponse)
                .toList();
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Busca un reclamo por id",
            description = "Detalle del reclamo con su estado, resolución y reembolso. Pueden "
                    + "llamarlo los roles CLIENTE, PROVEEDOR y ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reclamo encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un ticket con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<TicketResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(TicketMapper.toResponse(ticketService.obtenerPorId(id)));
    }

    @Operation(summary = "Lista los mensajes del hilo de mediación",
            description = "Hilo cronológico del reclamo (mensajes de cliente, proveedor y "
                    + "administrador). Pueden llamarlo los roles CLIENTE, PROVEEDOR y "
                    + "ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensajes ordenados por fecha"),
            @ApiResponse(responseCode = "404", description = "No existe un ticket con ese id")})
    @GetMapping("{id}/mensajes")
    public ResponseEntity<List<MensajeResponse>> listarMensajes(@PathVariable int id) {
        List<MensajeResponse> mensajes = ticketService.listarMensajes(id).stream()
                .map(TicketMapper::toResponse)
                .toList();
        return ResponseEntity.ok(mensajes);
    }

    /**
     * Mediación: mensajes de cliente, proveedor y administrador.
     */
    @Operation(summary = "Agrega un mensaje al hilo de mediación",
            description = "Mediación del reclamo: la primera intervención del ADMINISTRADOR "
                    + "deja el ticket EN_MEDIACION; un ticket cerrado no admite mensajes. "
                    + "Pueden llamarlo los roles CLIENTE, PROVEEDOR y ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje agregado al hilo"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe un ticket con ese id"),
            @ApiResponse(responseCode = "409", description = "El ticket está cerrado y no admite mensajes")})
    @PostMapping("{id}/mensajes")
    public ResponseEntity<MensajeResponse> agregarMensaje(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Mensaje del hilo de mediación",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"autorRol\": \"PROVEEDOR\", "
                                    + "\"mensaje\": \"Revisamos el lote del taladro: ofrecemos "
                                    + "reposición inmediata o reembolso total.\"}")))
            @Valid @RequestBody CreateMensajeRequest request) {
        TicketMensaje mensaje = ticketService.agregarMensaje(
                id, request.autorRol(), request.mensaje());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TicketMapper.toResponse(mensaje));
    }

    /**
     * Resolución del administrador; si autoriza reembolso, lo registra en
     * pagos y avisa al cliente vía notificaciones.
     */
    @Operation(summary = "Resuelve un reclamo (RESUELTO o RECHAZADO)",
            description = "Cierre de la mediación con constancia. Validaciones cruzadas "
                    + "WebClient: si autoriza reembolso, lo registra en el servicio pagos "
                    + "(reembolso total si se omite montoReembolso) y en todo caso avisa al "
                    + "cliente vía notificaciones. Solo puede llamarlo el rol ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reclamo resuelto; constancia y reembolso registrados"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe un ticket con ese id"),
            @ApiResponse(responseCode = "409", description = "Ticket ya cerrado, estado no terminal, "
                    + "reembolso sin estado RESUELTO, venta sin pago o pagos rechazó el reembolso"),
            @ApiResponse(responseCode = "503", description = "Servicio pagos o notificaciones no disponible")})
    @PatchMapping("{id}/resolver")
    public ResponseEntity<TicketResponse> resolver(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Resolución del administrador con reembolso opcional",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"estado\": \"RESUELTO\", "
                                    + "\"resolucion\": \"Se comprobó la falla de fábrica del "
                                    + "taladro; se autoriza el reembolso completo.\", "
                                    + "\"reembolsoAutorizado\": true, \"montoReembolso\": 49990}")))
            @Valid @RequestBody ResolverTicketRequest request) {
        Ticket ticket = ticketService.resolver(id, request.estado(), request.resolucion(),
                request.reembolsoAutorizado(), request.montoReembolso());
        return ResponseEntity.ok(TicketMapper.toResponse(ticket));
    }
}
