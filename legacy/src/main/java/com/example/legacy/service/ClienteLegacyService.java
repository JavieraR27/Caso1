package com.example.legacy.service;

import org.springframework.stereotype.Service;

import com.example.legacy.exception.BusinessConflictException;
import com.example.legacy.exception.ResourceNotFoundException;
import com.example.legacy.model.ClienteLegacy;
import com.example.legacy.repository.ClienteLegacyRepository;

@Service
public class ClienteLegacyService {

    private final ClienteLegacyRepository clienteLegacyRepository;

    public ClienteLegacyService(ClienteLegacyRepository clienteLegacyRepository) {
        this.clienteLegacyRepository = clienteLegacyRepository;
    }

    /**
     * Valida las credenciales de un cliente histórico.
     * 404 si el email no existe en el legacy; 409 si la password no coincide.
     */
    public ClienteLegacy validarCredenciales(String email, String password) {
        ClienteLegacy cliente = obtenerPorEmail(email);

        if (!cliente.getPassword().equals(password)) {
            throw new BusinessConflictException(
                    "Credenciales inválidas para el email: " + email);
        }
        return cliente;
    }

    public ClienteLegacy obtenerPorEmail(String email) {
        return clienteLegacyRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente legacy no encontrado para el email: " + email));
    }
}
