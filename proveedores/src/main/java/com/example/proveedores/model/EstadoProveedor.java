package com.example.proveedores.model;

/**
 * Ciclo de vida de la postulación de un proveedor:
 * nace POSTULADO y el administrador la resuelve a APROBADO o RECHAZADO.
 */
public enum EstadoProveedor {
    POSTULADO,
    APROBADO,
    RECHAZADO
}
