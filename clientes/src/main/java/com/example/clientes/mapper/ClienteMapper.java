package com.example.clientes.mapper;

import com.example.clientes.dto.ClienteResponse;
import com.example.clientes.dto.CreateClienteRequest;
import com.example.clientes.dto.DireccionResponse;
import com.example.clientes.model.Cliente;
import com.example.clientes.model.Direccion;

/**
 * Convierte entre los DTOs y las entidades del servicio de clientes.
 * El tipo, el legacyId y la fecha los define el service, no el mapper.
 */
public class ClienteMapper {

    public static Cliente toModel(CreateClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setEmail(request.email());
        cliente.setPassword(request.password());
        cliente.setNombre(request.nombre());
        cliente.setTelefono(request.telefono());
        return cliente;
    }

    public static ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getEmail(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getTipo(),
                cliente.getLegacyId(),
                cliente.getFechaCreacion());
    }

    public static DireccionResponse toResponse(Direccion direccion) {
        return new DireccionResponse(
                direccion.getId(),
                direccion.getCliente().getId(),
                direccion.getAlias(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getComuna(),
                direccion.getRegion());
    }
}
