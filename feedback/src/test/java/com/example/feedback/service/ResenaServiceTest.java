package com.example.feedback.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.feedback.dto.PromedioResponse;
import com.example.feedback.exception.BusinessConflictException;
import com.example.feedback.exception.ResourceNotFoundException;
import com.example.feedback.model.Resena;
import com.example.feedback.repository.ResenaRepository;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private WebClient ventasWebClient;

    @Mock
    private WebClient productosWebClient;

    @InjectMocks
    private ResenaService resenaService;

    private Resena resena(int calificacion) {
        return new Resena(0, 1, 5, 10, calificacion, "buen producto", LocalDateTime.now());
    }

    @Test
    void promedio_conResenas_calculaPromedioYTotal() {
        when(resenaRepository.findByProductoId(1))
                .thenReturn(List.of(resena(5), resena(4), resena(3)));

        PromedioResponse promedio = resenaService.promedio(1);

        assertThat(promedio.promedio()).isEqualTo(4.0);
        assertThat(promedio.total()).isEqualTo(3);
    }

    @Test
    void promedio_sinResenas_devuelveCero() {
        when(resenaRepository.findByProductoId(1)).thenReturn(List.of());

        PromedioResponse promedio = resenaService.promedio(1);

        assertThat(promedio.promedio()).isEqualTo(0.0);
        assertThat(promedio.total()).isZero();
    }

    @Test
    void crear_resenaDuplicada_lanza409() {
        when(resenaRepository.existsByClienteIdAndProductoIdAndVentaId(5, 1, 10))
                .thenReturn(true);

        assertThatThrownBy(() -> resenaService.crear(resena(5)))
                .isInstanceOf(BusinessConflictException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void eliminar_inexistente_lanza404() {
        when(resenaRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> resenaService.eliminar(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
