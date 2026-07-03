package com.example.proveedores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.proveedores.exception.BusinessConflictException;
import com.example.proveedores.model.DocumentoProveedor;
import com.example.proveedores.model.EstadoProveedor;
import com.example.proveedores.model.Proveedor;
import com.example.proveedores.repository.DocumentoProveedorRepository;
import com.example.proveedores.repository.ProveedorRepository;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private DocumentoProveedorRepository documentoProveedorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor condor(EstadoProveedor estado) {
        Proveedor p = new Proveedor();
        p.setId(1);
        p.setRut("76543210-K");
        p.setRazonSocial("Ferretería Cóndor");
        p.setEmail("condor@ferreteria.cl");
        p.setPassword("secreta");
        p.setTelefono("+56911111111");
        p.setEstado(estado);
        p.setFechaPostulacion(LocalDateTime.now());
        return p;
    }

    @Test
    void postular_rutDuplicado_lanza409() {
        when(proveedorRepository.existsByRut("76543210-K")).thenReturn(true);

        assertThatThrownBy(() -> proveedorService.postular(condor(null)))
                .isInstanceOf(BusinessConflictException.class);
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void postular_ok_quedaPostuladoConPasswordCifrada() {
        when(proveedorRepository.existsByRut(anyString())).thenReturn(false);
        when(proveedorRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("secreta")).thenReturn("$2a$hash");
        when(proveedorRepository.save(any(Proveedor.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Proveedor guardado = proveedorService.postular(condor(null));

        assertThat(guardado.getEstado()).isEqualTo(EstadoProveedor.POSTULADO);
        assertThat(guardado.getPassword()).isEqualTo("$2a$hash");
        assertThat(guardado.getFechaPostulacion()).isNotNull();
    }

    @Test
    void cambiarEstado_postulacionYaResuelta_lanza409() {
        when(proveedorRepository.findById(1))
                .thenReturn(Optional.of(condor(EstadoProveedor.APROBADO)));

        assertThatThrownBy(() -> proveedorService.cambiarEstado(
                1, EstadoProveedor.RECHAZADO, "obs"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void cambiarEstado_rechazoSinObservaciones_lanza409() {
        when(proveedorRepository.findById(1))
                .thenReturn(Optional.of(condor(EstadoProveedor.POSTULADO)));

        assertThatThrownBy(() -> proveedorService.cambiarEstado(
                1, EstadoProveedor.RECHAZADO, "  "))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void cambiarEstado_aprobar_ok() {
        when(proveedorRepository.findById(1))
                .thenReturn(Optional.of(condor(EstadoProveedor.POSTULADO)));
        when(proveedorRepository.save(any(Proveedor.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Proveedor aprobado = proveedorService.cambiarEstado(
                1, EstadoProveedor.APROBADO, "todo en regla");

        assertThat(aprobado.getEstado()).isEqualTo(EstadoProveedor.APROBADO);
        assertThat(aprobado.getFechaResolucion()).isNotNull();
    }

    @Test
    void agregarDocumento_postulacionResuelta_lanza409() {
        when(proveedorRepository.findById(1))
                .thenReturn(Optional.of(condor(EstadoProveedor.RECHAZADO)));

        assertThatThrownBy(() -> proveedorService.agregarDocumento(1, new DocumentoProveedor()))
                .isInstanceOf(BusinessConflictException.class);
        verify(documentoProveedorRepository, never()).save(any());
    }
}
