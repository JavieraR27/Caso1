package com.example.tickets.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.tickets.dto.PagoResponse;
import com.example.tickets.dto.VentaResponse;
import com.example.tickets.exception.BusinessConflictException;
import com.example.tickets.exception.ResourceNotFoundException;
import com.example.tickets.exception.ServiceUnavailableException;
import com.example.tickets.model.AutorRol;
import com.example.tickets.model.EstadoTicket;
import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketMensaje;
import com.example.tickets.repository.TicketMensajeRepository;
import com.example.tickets.repository.TicketRepository;

import reactor.core.publisher.Mono;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository ticketMensajeRepository;
    private final WebClient ventasWebClient;
    private final WebClient pagosWebClient;
    private final WebClient notificacionesWebClient;

    public TicketService(TicketRepository ticketRepository,
            TicketMensajeRepository ticketMensajeRepository,
            @Qualifier("ventasWebClient") WebClient ventasWebClient,
            @Qualifier("pagosWebClient") WebClient pagosWebClient,
            @Qualifier("notificacionesWebClient") WebClient notificacionesWebClient) {
        this.ticketRepository = ticketRepository;
        this.ticketMensajeRepository = ticketMensajeRepository;
        this.ventasWebClient = ventasWebClient;
        this.pagosWebClient = pagosWebClient;
        this.notificacionesWebClient = notificacionesWebClient;
    }

    /**
     * Abre un reclamo. Validación cruzada: la compra debe ser real (existe
     * en ventas); el cliente y el proveedor se toman de la venta.
     */
    public Ticket crear(int ventaId, Ticket datos) {
        VentaResponse venta = obtenerVenta(ventaId);

        if (venta.detalles() == null || venta.detalles().isEmpty()) {
            throw new BusinessConflictException(
                    "La venta " + ventaId + " no tiene líneas reclamables");
        }

        datos.setVentaId(ventaId);
        datos.setClienteId(venta.clienteId());
        datos.setProveedorId(venta.detalles().get(0).proveedorId());
        datos.setEstado(EstadoTicket.ABIERTO);
        datos.setReembolsoAutorizado(false);
        datos.setFechaApertura(LocalDateTime.now());
        return ticketRepository.save(datos);
    }

    public Ticket obtenerPorId(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket no encontrado para id: " + id));
    }

    public List<Ticket> listar(EstadoTicket estado, Integer clienteId) {
        if (estado != null && clienteId != null) {
            return ticketRepository.findByEstadoAndClienteId(estado, clienteId);
        }
        if (estado != null) {
            return ticketRepository.findByEstado(estado);
        }
        if (clienteId != null) {
            return ticketRepository.findByClienteId(clienteId);
        }
        return ticketRepository.findAll();
    }

    /**
     * Mensaje del hilo. Un ticket cerrado no admite mensajes; la primera
     * intervención del administrador deja el ticket EN_MEDIACION.
     */
    public TicketMensaje agregarMensaje(int ticketId, AutorRol autorRol, String texto) {
        Ticket ticket = obtenerPorId(ticketId);

        if (ticket.getEstado().esCerrado()) {
            throw new BusinessConflictException(
                    "El ticket " + ticketId + " está cerrado (" + ticket.getEstado()
                            + ") y no admite mensajes");
        }
        if (autorRol == AutorRol.ADMINISTRADOR && ticket.getEstado() == EstadoTicket.ABIERTO) {
            ticket.setEstado(EstadoTicket.EN_MEDIACION);
            ticketRepository.save(ticket);
        }

        TicketMensaje mensaje = new TicketMensaje();
        mensaje.setTicket(ticket);
        mensaje.setAutorRol(autorRol);
        mensaje.setMensaje(texto);
        mensaje.setFecha(LocalDateTime.now());
        return ticketMensajeRepository.save(mensaje);
    }

    public List<TicketMensaje> listarMensajes(int ticketId) {
        obtenerPorId(ticketId);
        return ticketMensajeRepository.findByTicketIdOrderByFechaAsc(ticketId);
    }

    /**
     * Resolución del administrador. Deja constancia (estado + texto), y si
     * autoriza reembolso lo registra en pagos y avisa al cliente.
     */
    public Ticket resolver(int id, EstadoTicket nuevoEstado, String resolucion,
            boolean reembolsoAutorizado, Integer montoReembolso) {
        Ticket ticket = obtenerPorId(id);

        if (ticket.getEstado().esCerrado()) {
            throw new BusinessConflictException(
                    "El ticket " + id + " ya fue resuelto (" + ticket.getEstado() + ")");
        }
        if (!nuevoEstado.esCerrado()) {
            throw new BusinessConflictException(
                    "La resolución debe dejar el ticket RESUELTO o RECHAZADO");
        }
        if (reembolsoAutorizado && nuevoEstado != EstadoTicket.RESUELTO) {
            throw new BusinessConflictException(
                    "Solo un ticket RESUELTO puede autorizar reembolso");
        }

        ticket.setEstado(nuevoEstado);
        ticket.setResolucion(resolucion);
        ticket.setReembolsoAutorizado(reembolsoAutorizado);
        ticket.setFechaResolucion(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        if (reembolsoAutorizado) {
            registrarReembolso(ticket, montoReembolso);
        }
        notificarResolucion(ticket);
        return ticket;
    }

    // ---- Llamadas WebClient ----

    private VentaResponse obtenerVenta(int ventaId) {
        try {
            return ventasWebClient.get()
                    .uri("/api/v1/ventas/{id}", ventaId)
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Venta no encontrada para id: " + ventaId)))
                    .bodyToMono(VentaResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio ventas no disponible", e);
        }
    }

    private void registrarReembolso(Ticket ticket, Integer montoReembolso) {
        PagoResponse pago = obtenerPagoDeVenta(ticket.getVentaId());
        int monto = montoReembolso != null ? montoReembolso : pago.monto();

        try {
            pagosWebClient.post()
                    .uri("/api/v1/pagos/{id}/reembolsos", pago.id())
                    .bodyValue(new ReembolsoRequest(ticket.getId(), monto,
                            "Reclamo #" + ticket.getId() + ": " + ticket.getResolucion()))
                    .retrieve()
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Pagos rechazó el reembolso del ticket " + ticket.getId())))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio pagos no disponible", e);
        }
    }

    private PagoResponse obtenerPagoDeVenta(int ventaId) {
        List<PagoResponse> pagos;
        try {
            pagos = pagosWebClient.get()
                    .uri(uri -> uri.path("/api/v1/pagos").queryParam("ventaId", ventaId).build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PagoResponse>>() {})
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio pagos no disponible", e);
        }
        if (pagos == null || pagos.isEmpty()) {
            throw new BusinessConflictException(
                    "La venta " + ventaId + " no tiene pago registrado; no hay qué reembolsar");
        }
        return pagos.get(0);
    }

    private void notificarResolucion(Ticket ticket) {
        try {
            notificacionesWebClient.post()
                    .uri("/api/v1/notificaciones")
                    .bodyValue(new NotificacionRequest(
                            "CLIENTE", ticket.getClienteId(), "RESOLUCION_RECLAMO",
                            "Tu reclamo fue " + ticket.getEstado(),
                            "El reclamo #" + ticket.getId() + " quedó " + ticket.getEstado()
                                    + (ticket.isReembolsoAutorizado()
                                            ? " con reembolso autorizado." : ".")
                                    + " Resolución: " + ticket.getResolucion(),
                            ticket.getId()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio notificaciones no disponible", e);
        }
    }

    // Cuerpos de las llamadas salientes (DTOs locales del consumidor)
    private record ReembolsoRequest(Integer ticketId, int monto, String motivo) {}
    private record NotificacionRequest(String destinatarioTipo, int destinatarioId,
            String tipo, String asunto, String mensaje, Integer referenciaId) {}
}
