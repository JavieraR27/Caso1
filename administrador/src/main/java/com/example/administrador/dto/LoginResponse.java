package com.example.administrador.dto;


public record LoginResponse(
        String token,
        AdministradorResponse administrador) {
}
