package com.example.despacho.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.despacho.dto.VentaResponse;
import com.example.despacho.exception.BusinessConflictException;
import com.example.despacho.exception.ResourceNotFoundException;
import com.example.despacho.exception.ServiceUnavailableException;
import com.example.despacho.model.EstadoEnvio;
import com.example.despacho.model.HistorialEstado;
import com.example.despacho.model.Seguimiento;
import com.example.despacho.repository.HistorialEstadoRepository;
import com.example.despacho.repository.SeguimientoRepository;

import reactor.core.publisher.Mono;

@Service
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final WebClient ventasWebClient;
    private final WebClient notificacionesWebClient;

    public SeguimientoService(SeguimientoRepository seguimientoRepository,
            HistorialEstadoRepository historialEstadoRepository,
            @Qualifier("ventasWebClient") WebClient ventasWebClient,
            @Qualifier("notificacionesWebClient") WebClient notificacionesWebClient) {
        this.seguimientoRepository = seguimientoRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.ventasWebClient = ventasWebClient;
        this.notificacionesWebClient = notificacionesWebClient;
    }

    /**
     * Crea el seguimiento de una venta PAGADA (validación cruzada contra
     * ventas). El cliente y el proveedor se toman de la venta; el proveedor
     * es el de la primera línea (envío multi-proveedor queda en backlog).
     */
    public Seguimiento crear(int ventaId, String direccionEntrega) {
        if (seguimientoRepository.existsByVentaId(ventaId)) {
            throw new BusinessConflictException(
                    "Ya existe un seguimiento para la venta: " + ventaId);
        }

        VentaResponse venta = obtenerVenta(ventaId);
        if (!"PAGADA".equals(venta.estado())) {
            throw new BusinessConflictException(
                    "Solo se crea seguimiento de una venta PAGADA; la venta "
                            + ventaId + " está " + venta.estado());
        }
        if (venta.detalles() == null || venta.detalles().isEmpty()) {
            throw new BusinessConflictException(
                    "La venta " + ventaId + " no tiene líneas para despachar");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Seguimiento seguimiento = new Seguimiento();
        seguimiento.setVentaId(ventaId);
        seguimiento.setClienteId(venta.clienteId());
        seguimiento.setProveedorId(venta.detalles().get(0).proveedorId());
        seguimiento.setEstadoActual(EstadoEnvio.PENDIENTE);
        seguimiento.setDireccionEntrega(direccionEntrega);
        seguimiento.setFechaCreacion(ahora);
        seguimiento.setFechaActualizacion(ahora);
        seguimiento = seguimientoRepository.save(seguimiento);

        seguimiento.setNumeroSeguimiento(String.format("ENV-%08d", seguimiento.getId()));
        seguimiento = seguimientoRepository.save(seguimiento);

        registrarHistorial(seguimiento, EstadoEnvio.PENDIENTE, "Seguimiento creado");
        return seguimiento;
    }

    /**
     * Cambio de estado ("marcar enviado" del vendedor). Sin cambios desde un
     * estado terminal; cada cambio queda en el historial; al pasar a ENVIADO
     * se avisa al cliente vía notificaciones.
     */
    public Seguimiento cambiarEstado(int id, EstadoEnvio nuevoEstado, String comentario) {
        Seguimiento seguimiento = obtenerPorId(id);

        if (seguimiento.getEstadoActual().esTerminal()) {
            throw new BusinessConflictException(
                    "El envío " + id + " está en estado terminal ("
                            + seguimiento.getEstadoActual() + ") y no admite cambios");
        }
        if (seguimiento.getEstadoActual() == nuevoEstado) {
            throw new BusinessConflictException(
                    "El envío " + id + " ya está en estado " + nuevoEstado);
        }

        seguimiento.setEstadoActual(nuevoEstado);
        seguimiento.setFechaActualizacion(LocalDateTime.now());
        seguimiento = seguimientoRepository.save(seguimiento);

        registrarHistorial(seguimiento, nuevoEstado, comentario);

        if (nuevoEstado == EstadoEnvio.ENVIADO) {
            notificarDespacho(seguimiento);
        }
        return seguimiento;
    }

    public Seguimiento obtenerPorId(int id) {
        return seguimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seguimiento no encontrado para id: " + id));
    }

    public List<Seguimiento> listar(Integer ventaId, Integer clienteId, Integer proveedorId) {
        if (ventaId != null) {
            return seguimientoRepository.findByVentaId(ventaId).map(List::of).orElse(List.of());
        }
        if (clienteId != null) {
            return seguimientoRepository.findByClienteId(clienteId);
        }
        if (proveedorId != null) {
            return seguimientoRepository.findByProveedorId(proveedorId);
        }
        return seguimientoRepository.findAll();
    }

    public List<HistorialEstado> historial(int seguimientoId) {
        obtenerPorId(seguimientoId);
        return historialEstadoRepository.findBySeguimientoIdOrderByFechaCambioAsc(seguimientoId);
    }

    private void registrarHistorial(Seguimiento seguimiento, EstadoEnvio estado, String comentario) {
        HistorialEstado registro = new HistorialEstado();
        registro.setSeguimiento(seguimiento);
        registro.setEstado(estado);
        registro.setComentario(comentario);
        registro.setFechaCambio(LocalDateTime.now());
        historialEstadoRepository.save(registro);
    }

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

    private void notificarDespacho(Seguimiento seguimiento) {
        try {
            notificacionesWebClient.post()
                    .uri("/api/v1/notificaciones")
                    .bodyValue(new NotificacionRequest(
                            "CLIENTE", seguimiento.getClienteId(), "DESPACHO",
                            "Tu pedido va en camino",
                            "El pedido de tu compra #" + seguimiento.getVentaId()
                                    + " fue despachado (seguimiento "
                                    + seguimiento.getNumeroSeguimiento() + ").",
                            seguimiento.getVentaId()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio notificaciones no disponible", e);
        }
    }

    /** Cuerpo del POST a notificaciones (DTO local del consumidor). */
    private record NotificacionRequest(String destinatarioTipo, int destinatarioId,
            String tipo, String asunto, String mensaje, Integer referenciaId) {}
}
