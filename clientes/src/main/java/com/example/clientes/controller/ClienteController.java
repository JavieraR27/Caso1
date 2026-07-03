package com.example.clientes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clientes.dto.ClienteResponse;
import com.example.clientes.dto.CreateClienteRequest;
import com.example.clientes.dto.CreateDireccionRequest;
import com.example.clientes.dto.DireccionResponse;
import com.example.clientes.dto.LoginRequest;
import com.example.clientes.dto.LoginResponse;
import com.example.clientes.dto.UpdateClienteRequest;
import com.example.clientes.mapper.ClienteMapper;
import com.example.clientes.model.Cliente;
import com.example.clientes.model.Direccion;
import com.example.clientes.security.JwtUtil;
import com.example.clientes.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Clientes",
        description = "Registro, login con migración desde el sistema legacy, perfil y "
                + "direcciones de despacho de los clientes del marketplace Paris")
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final JwtUtil jwtUtil;

    public ClienteController(ClienteService clienteService, JwtUtil jwtUtil) {
        this.clienteService = clienteService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Registra un cliente nuevo",
            description = "Alta directa de un cliente del marketplace (tipo NUEVO). "
                    + "Endpoint público: no requiere token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente registrado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con ese email")})
    @PostMapping
    public ResponseEntity<ClienteResponse> registrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del cliente nuevo",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"email\": \"pedro.soto@gmail.com\", \"password\": \"Secreta123\", "
                                    + "\"nombre\": \"Pedro Soto\", \"telefono\": \"+56912345678\"}")))
            @Valid @RequestBody CreateClienteRequest request) {
        Cliente cliente = clienteService.registrar(ClienteMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClienteMapper.toResponse(cliente));
    }

    /**
     * Login: valida local y, si el email no existe, contra el legacy
     * (migra al cliente histórico en su primer ingreso).
     */
    @Operation(summary = "Autentica un cliente y entrega su JWT",
            description = "Valida primero contra la base local; si el email no existe, valida las "
                    + "credenciales vía WebClient contra el servicio legacy y, de ser correctas, "
                    + "migra al cliente histórico (tipo MIGRADO) sin re-registro. "
                    + "Endpoint público: no requiere token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto; token JWT y datos del cliente"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El email no existe ni local ni en el legacy"),
            @ApiResponse(responseCode = "409", description = "La password no coincide"),
            @ApiResponse(responseCode = "503", description = "Servicio legacy no disponible")})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del cliente (nuevo o histórico)",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"email\": \"cliente1@paris.cl\", \"password\": \"pass1\"}")))
            @Valid @RequestBody LoginRequest request) {
        Cliente cliente = clienteService.login(request.email(), request.password());
        String token = jwtUtil.generar(String.valueOf(cliente.getId()), "CLIENTE");
        return ResponseEntity.ok(new LoginResponse(token, ClienteMapper.toResponse(cliente)));
    }

    @Operation(summary = "Busca un cliente por id",
            description = "Perfil del cliente (sin password). Lo consumen otros servicios para "
                    + "validaciones cruzadas (p. ej. ventas). Requiere token de rol CLIENTE, "
                    + "ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe cliente con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(ClienteMapper.toResponse(clienteService.obtenerPorId(id)));
    }

    @Operation(summary = "Actualiza el perfil de un cliente",
            description = "Solo nombre y teléfono (el email y la password no se editan en EP2). "
                    + "Requiere token de rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe cliente con ese id")})
    @PutMapping("{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos del perfil",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"nombre\": \"Pedro Soto Rojas\", \"telefono\": \"+56987654321\"}")))
            @Valid @RequestBody UpdateClienteRequest request) {
        Cliente cliente = clienteService.actualizar(id, request.nombre(), request.telefono());
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
    }

    @Operation(summary = "Lista las direcciones de despacho de un cliente",
            description = "Direcciones registradas para el despacho de compras (p. ej. taladros y "
                    + "herramientas). Requiere token de rol CLIENTE o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de direcciones (puede ser vacío)"),
            @ApiResponse(responseCode = "404", description = "No existe cliente con ese id")})
    @GetMapping("{id}/direcciones")
    public ResponseEntity<List<DireccionResponse>> listarDirecciones(@PathVariable int id) {
        List<DireccionResponse> direcciones = clienteService.listarDirecciones(id).stream()
                .map(ClienteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(direcciones);
    }

    @Operation(summary = "Agrega una dirección de despacho a un cliente",
            description = "Registra una nueva dirección con alias para futuras compras. "
                    + "Requiere token de rol CLIENTE o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dirección registrada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe cliente con ese id")})
    @PostMapping("{id}/direcciones")
    public ResponseEntity<DireccionResponse> agregarDireccion(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nueva dirección de despacho",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"alias\": \"Casa\", \"calle\": \"Av. Providencia\", "
                                    + "\"numero\": \"1234\", \"comuna\": \"Providencia\", "
                                    + "\"region\": \"Metropolitana\"}")))
            @Valid @RequestBody CreateDireccionRequest request) {
        Direccion direccion = new Direccion();
        direccion.setAlias(request.alias());
        direccion.setCalle(request.calle());
        direccion.setNumero(request.numero());
        direccion.setComuna(request.comuna());
        direccion.setRegion(request.region());

        Direccion guardada = clienteService.agregarDireccion(id, direccion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClienteMapper.toResponse(guardada));
    }
}
