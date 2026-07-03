package com.example.legacy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.legacy.exception.BusinessConflictException;
import com.example.legacy.exception.ResourceNotFoundException;
import com.example.legacy.model.ClienteLegacy;
import com.example.legacy.repository.ClienteLegacyRepository;

@ExtendWith(MockitoExtension.class)
class ClienteLegacyServiceTest {

    @Mock
    private ClienteLegacyRepository clienteLegacyRepository;

    @InjectMocks
    private ClienteLegacyService clienteLegacyService;

    private final ClienteLegacy maria = new ClienteLegacy(
            1, "10000001-1", "maria@paris.cl", "pass1", "María", LocalDate.of(2015, 1, 1));

    @Test
    void validarCredenciales_ok_devuelveElCliente() {
        when(clienteLegacyRepository.findByEmail("maria@paris.cl"))
                .thenReturn(Optional.of(maria));

        ClienteLegacy resultado = clienteLegacyService.validarCredenciales(
                "maria@paris.cl", "pass1");

        assertThat(resultado.getNombre()).isEqualTo("María");
    }

    @Test
    void validarCredenciales_emailInexistente_lanza404() {
        when(clienteLegacyRepository.findByEmail("nadie@paris.cl"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteLegacyService.validarCredenciales(
                "nadie@paris.cl", "pass1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validarCredenciales_passwordIncorrecta_lanza409() {
        when(clienteLegacyRepository.findByEmail("maria@paris.cl"))
                .thenReturn(Optional.of(maria));

        assertThatThrownBy(() -> clienteLegacyService.validarCredenciales(
                "maria@paris.cl", "otra"))
                .isInstanceOf(BusinessConflictException.class);
    }
}
