package com.example.proveedores.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de la postulación de un nuevo proveedor.
 */
public record CreateProveedorRequest(
        @NotBlank(message = "El rut no puede ser vacío")
        String rut,

        @NotBlank(message = "La razón social no puede ser vacía")
        String razonSocial,

        @NotBlank(message = "El email no puede ser vacío")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La password no puede ser vacía")
        String password,

        @NotBlank(message = "El teléfono no puede ser vacío")
        String telefono) {
}
