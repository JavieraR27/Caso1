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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "Administradores",
        description = "Cuentas del rol Administrador del marketplace Paris: alta y login "
                + "con emisión de JWT")
@RestController
@RequestMapping("/api/v1/administradores")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final JwtUtil jwtUtil;

    public AdministradorController(AdministradorService administradorService, JwtUtil jwtUtil) {
        this.administradorService = administradorService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Crea una cuenta de administrador",
            description = "Alta con password cifrada (BCrypt). Endpoint público en esta etapa "
                    + "(bootstrap del primer admin).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador creado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Ya existe ese username")})
    @PostMapping
    public ResponseEntity<AdministradorResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la cuenta de administrador",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"username\": \"admin\", \"password\": \"admin123\", "
                                    + "\"nombre\": \"Admin Paris\", \"email\": \"admin@paris.cl\"}")))
            @Valid @RequestBody CreateAdministradorRequest request) {
        Administrador admin = administradorService.crear(AdministradorMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdministradorMapper.toResponse(admin));
    }

    @Operation(summary = "Autentica un administrador y entrega su JWT",
            description = "Emite un token con rol ADMINISTRADOR para las acciones exclusivas "
                    + "(/api/v1/admin/**). Endpoint público: no requiere token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto; token JWT y datos de la cuenta"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe ese username"),
            @ApiResponse(responseCode = "409", description = "Password incorrecta")})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del administrador",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"username\": \"admin\", \"password\": \"admin123\"}")))
            @Valid @RequestBody LoginAdminRequest request) {
        Administrador admin = administradorService.login(request.username(), request.password());
        String token = jwtUtil.generar(String.valueOf(admin.getId()), "ADMINISTRADOR");
        return ResponseEntity.ok(new LoginResponse(token, AdministradorMapper.toResponse(admin)));
    }
}
