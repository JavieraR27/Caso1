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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Legacy",
        description = "Simulación del sistema legacy con los 5.000 clientes históricos de Almacenes Paris")
@RestController
@RequestMapping("/api/v1/legacy")
public class LegacyController {

    private final ClienteLegacyService clienteLegacyService;

    public LegacyController(ClienteLegacyService clienteLegacyService) {
        this.clienteLegacyService = clienteLegacyService;
    }

    @Operation(summary = "Valida credenciales de un cliente histórico",
            description = "Lo consume el servicio clientes durante el login: si las credenciales "
                    + "son válidas, el cliente histórico se migra al marketplace sin re-registrarse. "
                    + "Requiere token de rol INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas; datos del cliente (sin password)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El email no existe en el sistema legacy"),
            @ApiResponse(responseCode = "409", description = "La password no coincide")})
    @PostMapping("/validaciones")
    public ResponseEntity<ClienteLegacyResponse> validarCredenciales(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del cliente histórico",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"email\": \"cliente1@paris.cl\", \"password\": \"pass1\"}")))
            @Valid @RequestBody ValidacionRequest request) {
        ClienteLegacy cliente = clienteLegacyService.validarCredenciales(
                request.email(), request.password());
        return ResponseEntity.ok(ClienteLegacyMapper.toResponse(cliente));
    }

    @Operation(summary = "Busca un cliente histórico por email",
            description = "Datos del cliente legacy para la migración. Requiere token de rol INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "El email no existe en el sistema legacy")})
    @GetMapping("/clientes/{email}")
    public ResponseEntity<ClienteLegacyResponse> buscarPorEmail(@PathVariable String email) {
        ClienteLegacy cliente = clienteLegacyService.obtenerPorEmail(email);
        return ResponseEntity.ok(ClienteLegacyMapper.toResponse(cliente));
    }
}
