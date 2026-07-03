package com.example.pagos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.pagos.dto.VentaResponse;
import com.example.pagos.exception.BusinessConflictException;
import com.example.pagos.exception.ResourceNotFoundException;
import com.example.pagos.exception.ServiceUnavailableException;
import com.example.pagos.model.Comprobante;
import com.example.pagos.model.EstadoPago;
import com.example.pagos.model.EstadoReembolso;
import com.example.pagos.model.MedioPago;
import com.example.pagos.model.Pago;
import com.example.pagos.model.Reembolso;
import com.example.pagos.repository.ComprobanteRepository;
import com.example.pagos.repository.PagoRepository;
import com.example.pagos.repository.ReembolsoRepository;

import reactor.core.publisher.Mono;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final ReembolsoRepository reembolsoRepository;
    private final WebClient ventasWebClient;

    public PagoService(PagoRepository pagoRepository,
            ComprobanteRepository comprobanteRepository,
            ReembolsoRepository reembolsoRepository,
            @Qualifier("ventasWebClient") WebClient ventasWebClient) {
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.reembolsoRepository = reembolsoRepository;
        this.ventasWebClient = ventasWebClient;
    }

    /**
     * Registra el pago de una venta. Validación cruzada contra ventas: la
     * venta debe existir y estar PAGADA; el monto y el cliente se toman de
     * ella (no se confían al caller). Emite el comprobante automáticamente.
     */
    public Pago crear(int ventaId, MedioPago medioPago) {
        if (pagoRepository.existsByVentaId(ventaId)) {
            throw new BusinessConflictException(
                    "Ya existe un pago para la venta: " + ventaId);
        }

        VentaResponse venta = obtenerVenta(ventaId);
        if (!"PAGADA".equals(venta.estado())) {
            throw new BusinessConflictException(
                    "Solo se registra el pago de una venta PAGADA; la venta "
                            + ventaId + " está " + venta.estado());
        }

        Pago pago = new Pago();
        pago.setVentaId(ventaId);
        pago.setClienteId(venta.clienteId());
        pago.setMonto(venta.montoTotal());
        pago.setMedioPago(medioPago);
        pago.setEstado(EstadoPago.PAGADO);
        pago.setFechaPago(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        Comprobante comprobante = new Comprobante();
        comprobante.setPago(pago);
        comprobante.setFolio(String.format("F-%08d", pago.getId()));
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobanteRepository.save(comprobante);

        return pago;
    }

    public Pago obtenerPorId(int id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pago no encontrado para id: " + id));
    }

    public List<Pago> listar(Integer ventaId) {
        if (ventaId != null) {
            return pagoRepository.findByVentaId(ventaId).map(List::of).orElse(List.of());
        }
        return pagoRepository.findAll();
    }

    public Comprobante obtenerComprobante(int pagoId) {
        obtenerPorId(pagoId);
        return comprobanteRepository.findByPagoId(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comprobante no encontrado para el pago: " + pagoId));
    }

    /**
     * Registra el reembolso autorizado por el administrador (vía tickets).
     * Un solo reembolso por pago; el monto no puede exceder lo pagado.
     */
    public Reembolso crearReembolso(int pagoId, Integer ticketId, int monto, String motivo) {
        Pago pago = obtenerPorId(pagoId);

        if (pago.getEstado() != EstadoPago.PAGADO) {
            throw new BusinessConflictException(
                    "Solo se reembolsa un pago en estado PAGADO; el pago "
                            + pagoId + " está " + pago.getEstado());
        }
        if (monto > pago.getMonto()) {
            throw new BusinessConflictException(
                    "El reembolso ($" + monto + ") no puede exceder el monto pagado ($"
                            + pago.getMonto() + ")");
        }

        Reembolso reembolso = new Reembolso();
        reembolso.setPago(pago);
        reembolso.setTicketId(ticketId);
        reembolso.setMonto(monto);
        reembolso.setMotivo(motivo);
        reembolso.setEstado(EstadoReembolso.PROCESADO);
        reembolso.setFecha(LocalDateTime.now());
        reembolso = reembolsoRepository.save(reembolso);

        pago.setEstado(EstadoPago.REEMBOLSADO);
        pagoRepository.save(pago);

        return reembolso;
    }

    public List<Reembolso> listarReembolsos(int pagoId) {
        obtenerPorId(pagoId);
        return reembolsoRepository.findByPagoId(pagoId);
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
}
