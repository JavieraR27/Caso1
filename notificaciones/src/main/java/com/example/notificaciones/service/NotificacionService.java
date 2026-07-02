package com.example.notificaciones.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.notificaciones.exception.ResourceNotFoundException;
import com.example.notificaciones.model.EstadoNotificacion;
import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.model.TipoNotificacion;
import com.example.notificaciones.repository.NotificacionRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Registra el aviso. En EP2 el envío es simulado: la notificación queda
     * ENVIADA al momento de crearse (correo real en backlog EA3).
     */
    public Notificacion crear(Notificacion notificacion) {
        LocalDateTime ahora = LocalDateTime.now();
        notificacion.setEstado(EstadoNotificacion.ENVIADA);
        notificacion.setFechaCreacion(ahora);
        notificacion.setFechaEnvio(ahora);
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> listar(Integer destinatarioId, TipoNotificacion tipo) {
        if (destinatarioId != null && tipo != null) {
            return notificacionRepository.findByDestinatarioIdAndTipo(destinatarioId, tipo);
        }
        if (destinatarioId != null) {
            return notificacionRepository.findByDestinatarioId(destinatarioId);
        }
        if (tipo != null) {
            return notificacionRepository.findByTipo(tipo);
        }
        return notificacionRepository.findAll();
    }

    public Notificacion obtenerPorId(int id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación no encontrada para id: " + id));
    }
}
