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

import jakarta.validation.Valid;

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
    @PostMapping
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody CreateTicketRequest request) {
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
    @GetMapping
    public ResponseEntity<List<TicketResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoTicket estado,
            @RequestParam(name = "clienteId", required = false) Integer clienteId) {
        List<TicketResponse> tickets = ticketService.listar(estado, clienteId).stream()
                .map(TicketMapper::toResponse)
                .toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("{id}")
    public ResponseEntity<TicketResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(TicketMapper.toResponse(ticketService.obtenerPorId(id)));
    }

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
    @PostMapping("{id}/mensajes")
    public ResponseEntity<MensajeResponse> agregarMensaje(@PathVariable int id,
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
    @PatchMapping("{id}/resolver")
    public ResponseEntity<TicketResponse> resolver(@PathVariable int id,
            @Valid @RequestBody ResolverTicketRequest request) {
        Ticket ticket = ticketService.resolver(id, request.estado(), request.resolucion(),
                request.reembolsoAutorizado(), request.montoReembolso());
        return ResponseEntity.ok(TicketMapper.toResponse(ticket));
    }
}
