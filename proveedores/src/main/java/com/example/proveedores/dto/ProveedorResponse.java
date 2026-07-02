package com.example.proveedores.dto;

import java.time.LocalDateTime;

import com.example.proveedores.model.EstadoProveedor;

/**
 * Datos del proveedor que se exponen a otros servicios (productos, administrador).
 */
public record ProveedorResponse(
        int id,
        String rut,
        String razonSocial,
        String email,
        String telefono,
        EstadoProveedor estado,
        String observaciones,
        LocalDateTime fechaPostulacion,
        LocalDateTime fechaResolucion) {
}
