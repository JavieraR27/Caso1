package com.example.legacy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.legacy.dto.ClienteLegacyResponse;
import com.example.legacy.dto.ValidacionRequest;
import com.example.legacy.mapper.ClienteLegacyMapper;
import com.example.legacy.model.ClienteLegacy;
import com.example.legacy.service.ClienteLegacyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/legacy")
public class LegacyController {

    private final ClienteLegacyService clienteLegacyService;

    public LegacyController(ClienteLegacyService clienteLegacyService) {
        this.clienteLegacyService = clienteLegacyService;
    }

    /**
     * Valida credenciales de un cliente histórico (lo consume el servicio clientes
     * durante el login/migración). 200 con los datos, 404 si no existe, 409 si la
     * password no coincide.
     */
    @PostMapping("/validaciones")
    public ResponseEntity<ClienteLegacyResponse> validarCredenciales(
            @Valid @RequestBody ValidacionRequest request) {
        ClienteLegacy cliente = clienteLegacyService.validarCredenciales(
                request.email(), request.password());
        return ResponseEntity.ok(ClienteLegacyMapper.toResponse(cliente));
    }

    @GetMapping("/clientes/{email}")
    public ResponseEntity<ClienteLegacyResponse> buscarPorEmail(@PathVariable String email) {
        ClienteLegacy cliente = clienteLegacyService.obtenerPorEmail(email);
        return ResponseEntity.ok(ClienteLegacyMapper.toResponse(cliente));
    }
}
