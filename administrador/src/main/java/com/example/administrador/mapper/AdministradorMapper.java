package com.example.administrador.mapper;

import com.example.administrador.dto.AccionResponse;
import com.example.administrador.dto.AdministradorResponse;
import com.example.administrador.dto.CreateAdministradorRequest;
import com.example.administrador.dto.ReporteResponse;
import com.example.administrador.model.AccionAdmin;
import com.example.administrador.model.Administrador;
import com.example.administrador.model.ReporteSemanal;

/**
 * Convierte las entidades del servicio administrador a sus DTOs de salida
 * (nunca se expone la password).
 */
public class AdministradorMapper {

    public static Administrador toModel(CreateAdministradorRequest request) {
        Administrador admin = new Administrador();
        admin.setUsername(request.username());
        admin.setPassword(request.password());
        admin.setNombre(request.nombre());
        admin.setEmail(request.email());
        return admin;
    }

    public static AdministradorResponse toResponse(Administrador admin) {
        return new AdministradorResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getNombre(),
                admin.getEmail());
    }

    public static AccionResponse toResponse(AccionAdmin accion) {
        return new AccionResponse(
                accion.getId(),
                accion.getAdministrador().getId(),
                accion.getTipo(),
                accion.getReferenciaId(),
                accion.getObservaciones(),
                accion.getFecha());
    }

    public static ReporteResponse toResponse(ReporteSemanal reporte) {
        return new ReporteResponse(
                reporte.getId(),
                reporte.getAdministrador().getId(),
                reporte.getSemanaInicio(),
                reporte.getSemanaFin(),
                reporte.getTotalVentas(),
                reporte.getTotalComision(),
                reporte.getFechaGeneracion(),
                reporte.getCategorias().stream()
                        .map(c -> new ReporteResponse.ReporteCategoriaResponse(
                                c.getCategoria(), c.getUnidades(),
                                c.getMontoTotal(), c.getComisionTotal()))
                        .toList());
    }
}
