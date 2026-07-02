package com.example.proveedores.mapper;

import com.example.proveedores.dto.CreateProveedorRequest;
import com.example.proveedores.dto.DocumentoResponse;
import com.example.proveedores.dto.ProveedorResponse;
import com.example.proveedores.model.DocumentoProveedor;
import com.example.proveedores.model.Proveedor;

/**
 * Convierte entre los DTOs y las entidades del servicio de proveedores.
 * El estado inicial y las fechas los define el service, no el mapper.
 */
public class ProveedorMapper {

    public static Proveedor toModel(CreateProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRut(request.rut());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setEmail(request.email());
        proveedor.setTelefono(request.telefono());
        return proveedor;
    }

    public static ProveedorResponse toResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getRut(),
                proveedor.getRazonSocial(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.getEstado(),
                proveedor.getObservaciones(),
                proveedor.getFechaPostulacion(),
                proveedor.getFechaResolucion());
    }

    public static DocumentoResponse toResponse(DocumentoProveedor documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getProveedor().getId(),
                documento.getTipo(),
                documento.getNombreArchivo(),
                documento.getUrl(),
                documento.getFechaCarga());
    }
}
