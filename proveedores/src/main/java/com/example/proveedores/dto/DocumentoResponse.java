package com.example.proveedores.dto;

import java.time.LocalDateTime;

/**
 * Documento de la postulación con la referencia plana al proveedor
 * (evita serializar la entidad completa).
 */
public record DocumentoResponse(
        int id,
        int proveedorId,
        String tipo,
        String nombreArchivo,
        String url,
        LocalDateTime fechaCarga) {
}
