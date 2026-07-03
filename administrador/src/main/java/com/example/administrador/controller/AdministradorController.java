package com.example.administrador.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.administrador.dto.AdministradorResponse;
import com.example.administrador.dto.CreateAdministradorRequest;
import com.example.administrador.dto.LoginAdminRequest;
import com.example.administrador.dto.LoginResponse;
import com.example.administrador.mapper.AdministradorMapper;
import com.example.administrador.model.Administrador;
import com.example.administrador.security.JwtUtil;
import com.example.administrador.service.AdministradorService;

import jakarta.validation.Valid;

/**
 * Cuenta del administrador: alta y login con emisión de JWT (rol
 * ADMINISTRADOR) y password cifrada con BCrypt.
 */
@RestController
@RequestMapping("/api/v1/administradores")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final JwtUtil jwtUtil;

    public AdministradorController(AdministradorService administradorService, JwtUtil jwtUtil) {
        this.administradorService = administradorService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<AdministradorResponse> crear(
            @Valid @RequestBody CreateAdministradorRequest request) {
        Administrador admin = administradorService.crear(AdministradorMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdministradorMapper.toResponse(admin));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginAdminRequest request) {
        Administrador admin = administradorService.login(request.username(), request.password());
        String token = jwtUtil.generar(String.valueOf(admin.getId()), "ADMINISTRADOR");
        return ResponseEntity.ok(new LoginResponse(token, AdministradorMapper.toResponse(admin)));
    }
}
