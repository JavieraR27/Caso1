package com.example.proveedores.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Documento que el proveedor adjunta a su postulación.
 */
public record CreateDocumentoRequest(
        @NotBlank(message = "El tipo de documento no puede ser vacío")
        String tipo,

        @NotBlank(message = "El nombre del archivo no puede ser vacío")
        String nombreArchivo,

        @NotBlank(message = "La url del documento no puede ser vacía")
        String url) {
}
