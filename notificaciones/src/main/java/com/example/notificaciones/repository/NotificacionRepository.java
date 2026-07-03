package com.example.notificaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.notificaciones.model.DestinatarioTipo;
import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.model.TipoNotificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByDestinatarioId(int destinatarioId);

    List<Notificacion> findByDestinatarioIdAndTipo(int destinatarioId, TipoNotificacion tipo);

    List<Notificacion> findByTipo(TipoNotificacion tipo);

    List<Notificacion> findByDestinatarioTipoAndDestinatarioId(
            DestinatarioTipo destinatarioTipo, int destinatarioId);

    List<Notificacion> findByDestinatarioTipoAndDestinatarioIdAndTipo(
            DestinatarioTipo destinatarioTipo, int destinatarioId, TipoNotificacion tipo);
}
