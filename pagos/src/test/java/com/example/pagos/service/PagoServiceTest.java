package com.example.pagos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.pagos.exception.BusinessConflictException;
import com.example.pagos.model.EstadoPago;
import com.example.pagos.model.EstadoReembolso;
import com.example.pagos.model.MedioPago;
import com.example.pagos.model.Pago;
import com.example.pagos.model.Reembolso;
import com.example.pagos.repository.ComprobanteRepository;
import com.example.pagos.repository.PagoRepository;
import com.example.pagos.repository.ReembolsoRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private ReembolsoRepository reembolsoRepository;

    @Mock
    private WebClient ventasWebClient;

    @InjectMocks
    private PagoService pagoService;

    private Pago pago(EstadoPago estado) {
        return new Pago(1, 10, 5, 50000, MedioPago.TARJETA, estado, LocalDateTime.now());
    }

    @Test
    void crearReembolso_pagoNoEstaPagado_lanza409() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(EstadoPago.REEMBOLSADO)));

        assertThatThrownBy(() -> pagoService.crearReembolso(1, 7, 50000, "defectuoso"))
                .isInstanceOf(BusinessConflictException.class);
        verify(reembolsoRepository, never()).save(any());
    }

    @Test
    void crearReembolso_montoExcedeElPago_lanza409() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(EstadoPago.PAGADO)));

        assertThatThrownBy(() -> pagoService.crearReembolso(1, 7, 60000, "defectuoso"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void crearReembolso_ok_dejaElPagoReembolsado() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(EstadoPago.PAGADO)));
        when(reembolsoRepository.save(any(Reembolso.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.save(any(Pago.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Reembolso reembolso = pagoService.crearReembolso(1, 7, 50000, "producto defectuoso");

        assertThat(reembolso.getEstado()).isEqualTo(EstadoReembolso.PROCESADO);
        assertThat(reembolso.getPago().getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
        assertThat(reembolso.getTicketId()).isEqualTo(7);
    }
}
