package com.example.administrador.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.administrador.dto.ProveedorResponse;
import com.example.administrador.dto.VentaResponse;
import com.example.administrador.exception.BusinessConflictException;
import com.example.administrador.exception.ResourceNotFoundException;
import com.example.administrador.exception.ServiceUnavailableException;
import com.example.administrador.model.AccionAdmin;
import com.example.administrador.model.Administrador;
import com.example.administrador.model.ReporteCategoria;
import com.example.administrador.model.ReporteSemanal;
import com.example.administrador.model.TipoAccion;
import com.example.administrador.repository.AccionAdminRepository;
import com.example.administrador.repository.AdministradorRepository;
import com.example.administrador.repository.ReporteSemanalRepository;

import reactor.core.publisher.Mono;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final AccionAdminRepository accionAdminRepository;
    private final ReporteSemanalRepository reporteSemanalRepository;
    private final WebClient proveedoresWebClient;
    private final WebClient ventasWebClient;
    private final WebClient notificacionesWebClient;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(AdministradorRepository administradorRepository,
            AccionAdminRepository accionAdminRepository,
            ReporteSemanalRepository reporteSemanalRepository,
            @Qualifier("proveedoresWebClient") WebClient proveedoresWebClient,
            @Qualifier("ventasWebClient") WebClient ventasWebClient,
            @Qualifier("notificacionesWebClient") WebClient notificacionesWebClient,
            PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.accionAdminRepository = accionAdminRepository;
        this.reporteSemanalRepository = reporteSemanalRepository;
        this.proveedoresWebClient = proveedoresWebClient;
        this.ventasWebClient = ventasWebClient;
        this.notificacionesWebClient = notificacionesWebClient;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Cuenta del administrador ----

    public Administrador crear(Administrador admin) {
        if (administradorRepository.existsByUsername(admin.getUsername())) {
            throw new BusinessConflictException(
                    "Ya existe un administrador con el username: " + admin.getUsername());
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return administradorRepository.save(admin);
    }

    public Administrador login(String username, String password) {
        Administrador admin = administradorRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado: " + username));
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessConflictException(
                    "Credenciales inválidas para el administrador: " + username);
        }
        return admin;
    }

    public Administrador obtenerPorId(int id) {
        return administradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado para id: " + id));
    }

    // ---- Aprobación de proveedores ----

    /**
     * Bandeja de postulaciones pendientes (proxy a proveedores?estado=POSTULADO).
     */
    public List<ProveedorResponse> proveedoresPendientes() {
        try {
            ProveedorResponse[] pendientes = proveedoresWebClient.get()
                    .uri(uri -> uri.path("/api/v1/proveedores")
                            .queryParam("estado", "POSTULADO").build())
                    .retrieve()
                    .bodyToMono(ProveedorResponse[].class)
                    .block();
            return pendientes != null ? Arrays.asList(pendientes) : List.of();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio proveedores no disponible", e);
        }
    }

    /**
     * Resuelve la postulación: PATCH al servicio proveedores, registra la
     * acción en la auditoría y avisa al proveedor por notificaciones.
     */
    public AccionAdmin resolverProveedor(int proveedorId, int adminId, boolean aprobado,
            String observaciones) {
        Administrador admin = obtenerPorId(adminId);
        String nuevoEstado = aprobado ? "APROBADO" : "RECHAZADO";

        try {
            proveedoresWebClient.patch()
                    .uri("/api/v1/proveedores/{id}/estado", proveedorId)
                    .bodyValue(new CambioEstadoProveedor(nuevoEstado, observaciones))
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Proveedor no encontrado para id: " + proveedorId)))
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Proveedores rechazó la resolución del proveedor "
                                            + proveedorId
                                            + " (ya resuelto o rechazo sin observaciones)")))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio proveedores no disponible", e);
        }

        notificarProveedor(proveedorId, aprobado, observaciones);

        AccionAdmin accion = new AccionAdmin();
        accion.setAdministrador(admin);
        accion.setTipo(aprobado ? TipoAccion.APROBAR_PROVEEDOR : TipoAccion.RECHAZAR_PROVEEDOR);
        accion.setReferenciaId(proveedorId);
        accion.setObservaciones(observaciones);
        accion.setFecha(LocalDateTime.now());
        return accionAdminRepository.save(accion);
    }

    // ---- Reporte semanal por categoría ----

    /**
     * Genera y PERSISTE el reporte: agrega las ventas PAGADAS del rango
     * (obtenidas de ventas) usando el snapshot de categoría de cada línea.
     */
    public ReporteSemanal generarReporte(int adminId, LocalDate desde, LocalDate hasta) {
        Administrador admin = obtenerPorId(adminId);
        if (hasta.isBefore(desde)) {
            throw new BusinessConflictException(
                    "La fecha de fin del reporte no puede ser anterior a la de inicio");
        }

        List<VentaResponse> ventas = obtenerVentas(desde, hasta).stream()
                .filter(v -> "PAGADA".equals(v.estado()))
                .toList();

        Map<String, ReporteCategoria> porCategoria = new LinkedHashMap<>();
        int totalVentas = 0;
        int totalComision = 0;

        for (VentaResponse venta : ventas) {
            totalVentas += venta.montoTotal();
            totalComision += venta.comisionTotal();

            for (VentaResponse.DetalleVentaRemoto detalle : venta.detalles()) {
                ReporteCategoria acumulado = porCategoria.computeIfAbsent(
                        detalle.categoria(),
                        c -> new ReporteCategoria(0, c, 0, 0, 0));
                acumulado.setUnidades(acumulado.getUnidades() + detalle.cantidad());
                acumulado.setMontoTotal(acumulado.getMontoTotal() + detalle.subtotal());
                acumulado.setComisionTotal(acumulado.getComisionTotal() + detalle.comision());
            }
        }

        ReporteSemanal reporte = new ReporteSemanal();
        reporte.setAdministrador(admin);
        reporte.setSemanaInicio(desde);
        reporte.setSemanaFin(hasta);
        reporte.setTotalVentas(totalVentas);
        reporte.setTotalComision(totalComision);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.getCategorias().addAll(porCategoria.values());
        reporte = reporteSemanalRepository.save(reporte);

        AccionAdmin accion = new AccionAdmin();
        accion.setAdministrador(admin);
        accion.setTipo(TipoAccion.GENERAR_REPORTE);
        accion.setReferenciaId(reporte.getId());
        accion.setObservaciones("Reporte " + desde + " a " + hasta);
        accion.setFecha(LocalDateTime.now());
        accionAdminRepository.save(accion);

        return reporte;
    }

    public List<ReporteSemanal> listarReportes() {
        return reporteSemanalRepository.findAll();
    }

    public ReporteSemanal obtenerReporte(int id) {
        return reporteSemanalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reporte no encontrado para id: " + id));
    }

    public List<AccionAdmin> listarAcciones() {
        return accionAdminRepository.findAllByOrderByFechaDesc();
    }

    // ---- Llamadas WebClient ----

    private List<VentaResponse> obtenerVentas(LocalDate desde, LocalDate hasta) {
        try {
            VentaResponse[] ventas = ventasWebClient.get()
                    .uri(uri -> uri.path("/api/v1/ventas")
                            .queryParam("desde", desde.toString())
                            .queryParam("hasta", hasta.toString())
                            .build())
                    .retrieve()
                    .bodyToMono(VentaResponse[].class)
                    .block();
            return ventas != null ? Arrays.asList(ventas) : List.of();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio ventas no disponible", e);
        }
    }

    private void notificarProveedor(int proveedorId, boolean aprobado, String observaciones) {
        try {
            notificacionesWebClient.post()
                    .uri("/api/v1/notificaciones")
                    .bodyValue(new NotificacionRequest(
                            "PROVEEDOR", proveedorId, "APROBACION_PROVEEDOR",
                            aprobado ? "Postulación aprobada" : "Postulación rechazada",
                            aprobado
                                    ? "Tu postulación fue aprobada: ya puedes publicar tu catálogo."
                                    : "Tu postulación fue rechazada. Observaciones: "
                                            + observaciones,
                            proveedorId))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio notificaciones no disponible", e);
        }
    }

    // Cuerpos de las llamadas salientes (DTOs locales del consumidor)
    private record CambioEstadoProveedor(String estado, String observaciones) {}
    private record NotificacionRequest(String destinatarioTipo, int destinatarioId,
            String tipo, String asunto, String mensaje, Integer referenciaId) {}
}
