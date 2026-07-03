package com.example.administrador.dto;


public record ProveedorResponse(
        int id,
        String rut,
        String razonSocial,
        String email,
        String estado) {
}
