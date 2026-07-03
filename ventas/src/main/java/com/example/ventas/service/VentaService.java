package com.example.ventas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.ventas.dto.ClienteResponse;
import com.example.ventas.dto.DetalleVentaRequest;
import com.example.ventas.dto.ProductoResponse;
import com.example.ventas.exception.BusinessConflictException;
import com.example.ventas.exception.ResourceNotFoundException;
import com.example.ventas.exception.ServiceUnavailableException;
import com.example.ventas.model.DetalleVenta;
import com.example.ventas.model.EstadoVenta;
import com.example.ventas.model.Venta;
import com.example.ventas.repository.VentaRepository;

import reactor.core.publisher.Mono;

/**
 * Orquestador del marketplace: crea la orden con validaciones cruzadas
 * (clientes, productos), calcula la comisión y, al pagar, coordina el
 * descuento de stock, el pago, el seguimiento de despacho y el aviso.
 *
 * Consistencia eventual: la venta persiste su estado antes de orquestar; si
 * una llamada posterior falla, el error se responde tal cual (404/409/503)
 * sin revertir silenciosamente.
 */
@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final WebClient clientesWebClient;
    private final WebClient productosWebClient;
    private final WebClient pagosWebClient;
    private final WebClient despachoWebClient;
    private final WebClient notificacionesWebClient;
    private final int comisionPorcentaje;

    public VentaService(VentaRepository ventaRepository,
            @Qualifier("clientesWebClient") WebClient clientesWebClient,
            @Qualifier("productosWebClient") WebClient productosWebClient,
            @Qualifier("pagosWebClient") WebClient pagosWebClient,
            @Qualifier("despachoWebClient") WebClient despachoWebClient,
            @Qualifier("notificacionesWebClient") WebClient notificacionesWebClient,
            @Value("${paris.comision.porcentaje}") int comisionPorcentaje) {
        this.ventaRepository = ventaRepository;
        this.clientesWebClient = clientesWebClient;
        this.productosWebClient = productosWebClient;
        this.pagosWebClient = pagosWebClient;
        this.despachoWebClient = despachoWebClient;
        this.notificacionesWebClient = notificacionesWebClient;
        this.comisionPorcentaje = comisionPorcentaje;
    }

    /**
     * Crea la orden: valida el cliente y cada producto (existencia, estado,
     * stock), toma el precio VIGENTE del catálogo como snapshot y calcula
     * subtotales y comisión por línea.
     */
    public Venta crear(int clienteId, List<DetalleVentaRequest> lineas) {
        validarCliente(clienteId);

        Venta venta = new Venta();
        venta.setClienteId(clienteId);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.CREADA);

        int montoTotal = 0;
        int comisionTotal = 0;

        for (DetalleVentaRequest linea : lineas) {
            ProductoResponse producto = obtenerProducto(linea.productoId());

            if (!"ACTIVO".equals(producto.estado())) {
                throw new BusinessConflictException(
                        "El producto " + producto.id() + " no está ACTIVO en el catálogo");
            }
            if (producto.stock() < linea.cantidad()) {
                throw new BusinessConflictException(
                        "Stock insuficiente para el producto " + producto.id()
                                + ": disponible " + producto.stock()
                                + ", solicitado " + linea.cantidad());
            }

            int subtotal = producto.precioVigente() * linea.cantidad();
            int comision = subtotal * comisionPorcentaje / 100;

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProductoId(producto.id());
            detalle.setProveedorId(producto.proveedorId());
            detalle.setNombreProducto(producto.nombre());
            detalle.setCategoria(producto.categoriaNombre());
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(producto.precioVigente());
            detalle.setSubtotal(subtotal);
            detalle.setComision(comision);
            venta.getDetalles().add(detalle);

            montoTotal += subtotal;
            comisionTotal += comision;
        }

        venta.setMontoTotal(montoTotal);
        venta.setComisionTotal(comisionTotal);
        return ventaRepository.save(venta);
    }

    /**
     * Orquestación del pago: descuenta stock (si falla, la venta sigue
     * CREADA), marca PAGADA y luego coordina pagos, despacho y notificaciones.
     */
    public Venta pagar(int id, String medioPago, String direccionEntrega) {
        Venta venta = obtenerPorId(id);

        if (venta.getEstado() != EstadoVenta.CREADA) {
            throw new BusinessConflictException(
                    "Solo una venta CREADA puede pagarse; la venta " + id
                            + " está " + venta.getEstado());
        }

        // 1. Descuento de stock por línea (pre-condición: si falla, no se paga)
        for (DetalleVenta detalle : venta.getDetalles()) {
            descontarStock(detalle.getProductoId(), detalle.getCantidad());
        }

        // 2. Estado propio persistido antes de orquestar al resto
        venta.setEstado(EstadoVenta.PAGADA);
        venta = ventaRepository.save(venta);

        // 3. Pago (pagos valida de vuelta que la venta esté PAGADA)
        registrarPago(venta.getId(), medioPago);

        // 4. Seguimiento de despacho
        crearSeguimiento(venta.getId(), direccionEntrega);

        // 5. Aviso al cliente
        notificarCompra(venta);

        return venta;
    }

    public Venta obtenerPorId(int id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada para id: " + id));
    }

    /**
     * Filtros de listado: cliente (historial), proveedor (órdenes del
     * vendedor) o rango de fechas (insumo del reporte semanal).
     */
    public List<Venta> listar(Integer clienteId, Integer proveedorId,
            LocalDate desde, LocalDate hasta) {
        if (clienteId != null) {
            return ventaRepository.findByClienteId(clienteId);
        }
        if (proveedorId != null) {
            return ventaRepository.findByProveedorId(proveedorId);
        }
        if (desde != null && hasta != null) {
            return ventaRepository.findByFechaBetween(
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        }
        return ventaRepository.findAll();
    }

    // ---- Llamadas WebClient (validación cruzada y orquestación) ----

    private void validarCliente(int clienteId) {
        try {
            clientesWebClient.get()
                    .uri("/api/v1/clientes/{id}", clienteId)
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Cliente no encontrado para id: " + clienteId)))
                    .bodyToMono(ClienteResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio clientes no disponible", e);
        }
    }

    private ProductoResponse obtenerProducto(int productoId) {
        try {
            return productosWebClient.get()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Producto no encontrado para id: " + productoId)))
                    .bodyToMono(ProductoResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio productos no disponible", e);
        }
    }

    private void descontarStock(int productoId, int cantidad) {
        try {
            productosWebClient.patch()
                    .uri("/api/v1/productos/{id}/stock", productoId)
                    .bodyValue(new DescuentoStock(cantidad))
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Producto no encontrado para id: " + productoId)))
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Stock insuficiente para el producto " + productoId)))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio productos no disponible", e);
        }
    }

    private void registrarPago(int ventaId, String medioPago) {
        try {
            pagosWebClient.post()
                    .uri("/api/v1/pagos")
                    .bodyValue(new PagoRequest(ventaId, medioPago))
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Pagos no encontró la venta " + ventaId)))
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Pagos rechazó el pago de la venta " + ventaId)))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio pagos no disponible", e);
        }
    }

    private void crearSeguimiento(int ventaId, String direccionEntrega) {
        try {
            despachoWebClient.post()
                    .uri("/api/v1/envios")
                    .bodyValue(new EnvioRequest(ventaId, direccionEntrega))
                    .retrieve()
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Despacho rechazó el seguimiento de la venta " + ventaId)))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio despacho no disponible", e);
        }
    }

    private void notificarCompra(Venta venta) {
        try {
            notificacionesWebClient.post()
                    .uri("/api/v1/notificaciones")
                    .bodyValue(new NotificacionRequest(
                            "CLIENTE", venta.getClienteId(), "VENTA_CONFIRMADA",
                            "Compra confirmada",
                            "Tu compra #" + venta.getId() + " por $" + venta.getMontoTotal()
                                    + " fue confirmada y está en preparación.",
                            venta.getId()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio notificaciones no disponible", e);
        }
    }

    // Cuerpos de las llamadas salientes (DTOs locales del consumidor)
    private record DescuentoStock(int cantidad) {}
    private record PagoRequest(int ventaId, String medioPago) {}
    private record EnvioRequest(int ventaId, String direccionEntrega) {}
    private record NotificacionRequest(String destinatarioTipo, int destinatarioId,
            String tipo, String asunto, String mensaje, Integer referenciaId) {}
}
