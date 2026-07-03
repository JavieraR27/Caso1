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
import com.example.clientes.dto.UpdateClienteRequest;
import com.example.clientes.mapper.ClienteMapper;
import com.example.clientes.model.Cliente;
import com.example.clientes.model.Direccion;
import com.example.clientes.service.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> registrar(
            @Valid @RequestBody CreateClienteRequest request) {
        Cliente cliente = clienteService.registrar(ClienteMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClienteMapper.toResponse(cliente));
    }

    /**
     * Login: valida local y, si el email no existe, contra el legacy
     * (migra al cliente histórico en su primer ingreso).
     */
    @PostMapping("/login")
    public ResponseEntity<ClienteResponse> login(@Valid @RequestBody LoginRequest request) {
        Cliente cliente = clienteService.login(request.email(), request.password());
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
    }

    @GetMapping("{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(ClienteMapper.toResponse(clienteService.obtenerPorId(id)));
    }

    @PutMapping("{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable int id,
            @Valid @RequestBody UpdateClienteRequest request) {
        Cliente cliente = clienteService.actualizar(id, request.nombre(), request.telefono());
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
    }

    @GetMapping("{id}/direcciones")
    public ResponseEntity<List<DireccionResponse>> listarDirecciones(@PathVariable int id) {
        List<DireccionResponse> direcciones = clienteService.listarDirecciones(id).stream()
                .map(ClienteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(direcciones);
    }

    @PostMapping("{id}/direcciones")
    public ResponseEntity<DireccionResponse> agregarDireccion(@PathVariable int id,
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
