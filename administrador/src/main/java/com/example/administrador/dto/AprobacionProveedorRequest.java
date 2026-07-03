package com.example.administrador.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record AprobacionProveedorRequest(
        @NotNull(message = "El id del administrador no puede ser nulo")
        @Positive(message = "El id del administrador debe ser positivo")
        Integer adminId,

        @NotNull(message = "aprobado no puede ser nulo")
        Boolean aprobado,

        String observaciones) {
}
