package com.example.notificaciones.model;

/**
 * Estado del envío. En EP2 el envío es simulado, por lo que toda notificación
 * creada queda ENVIADA; PENDIENTE queda para el correo real del backlog.
 */
public enum EstadoNotificacion {
    PENDIENTE,
    ENVIADA
}
