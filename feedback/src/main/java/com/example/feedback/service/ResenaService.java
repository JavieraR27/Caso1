package com.example.feedback.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.feedback.dto.PromedioResponse;
import com.example.feedback.dto.VentaResponse;
import com.example.feedback.exception.BusinessConflictException;
import com.example.feedback.exception.ResourceNotFoundException;
import com.example.feedback.exception.ServiceUnavailableException;
import com.example.feedback.model.Resena;
import com.example.feedback.repository.ResenaRepository;

import reactor.core.publisher.Mono;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final WebClient ventasWebClient;
    private final WebClient productosWebClient;

    public ResenaService(ResenaRepository resenaRepository,
            @Qualifier("ventasWebClient") WebClient ventasWebClient,
            @Qualifier("productosWebClient") WebClient productosWebClient) {
        this.resenaRepository = resenaRepository;
        this.ventasWebClient = ventasWebClient;
        this.productosWebClient = productosWebClient;
    }

    /**
     * Publica una reseña con COMPRA VERIFICADA: la venta existe, pertenece
     * al cliente y contiene el producto reseñado; el producto además existe
     * en el catálogo. Una sola reseña por (cliente, producto, venta).
     */
    public Resena crear(Resena resena) {
        if (resenaRepository.existsByClienteIdAndProductoIdAndVentaId(
                resena.getClienteId(), resena.getProductoId(), resena.getVentaId())) {
            throw new BusinessConflictException(
                    "Ya existe una reseña del cliente " + resena.getClienteId()
                            + " para el producto " + resena.getProductoId()
                            + " en la venta " + resena.getVentaId());
        }

        VentaResponse venta = obtenerVenta(resena.getVentaId());
        if (venta.clienteId() != resena.getClienteId()) {
            throw new BusinessConflictException(
                    "La venta " + resena.getVentaId() + " no pertenece al cliente "
                            + resena.getClienteId());
        }
        boolean comprado = venta.detalles() != null && venta.detalles().stream()
                .anyMatch(d -> d.productoId() == resena.getProductoId());
        if (!comprado) {
            throw new BusinessConflictException(
                    "Compra no verificada: el producto " + resena.getProductoId()
                            + " no está en los detalles de la venta " + resena.getVentaId());
        }
        validarProductoExiste(resena.getProductoId());

        resena.setFechaCreacion(LocalDateTime.now());
        return resenaRepository.save(resena);
    }

    public List<Resena> listarPorProducto(int productoId) {
        return resenaRepository.findByProductoId(productoId);
    }

    /**
     * Reputación del producto: promedio (0 si no hay reseñas) y total.
     */
    public PromedioResponse promedio(int productoId) {
        List<Resena> resenas = resenaRepository.findByProductoId(productoId);
        double promedio = resenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
        return new PromedioResponse(productoId, promedio, resenas.size());
    }

    /**
     * Moderación del administrador: elimina una reseña inapropiada.
     */
    public void eliminar(int id) {
        if (!resenaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reseña no encontrada para id: " + id);
        }
        resenaRepository.deleteById(id);
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

    private void validarProductoExiste(int productoId) {
        try {
            productosWebClient.get()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Producto no encontrado para id: " + productoId)))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio productos no disponible", e);
        }
    }
}
