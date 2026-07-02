package com.example.legacy.mapper;

import com.example.legacy.dto.ClienteLegacyResponse;
import com.example.legacy.model.ClienteLegacy;

/**
 * Convierte la entidad ClienteLegacy a su DTO de salida (nunca se expone la password).
 */
public class ClienteLegacyMapper {

    public static ClienteLegacyResponse toResponse(ClienteLegacy cliente) {
        return new ClienteLegacyResponse(
                cliente.getId(),
                cliente.getRut(),
                cliente.getEmail(),
                cliente.getNombre(),
                cliente.getFechaRegistro());
    }
}
