package com.example.clientes.model;

/**
 * Origen del cliente: NUEVO se registró en el marketplace; MIGRADO viene del
 * sistema legacy (conserva sus credenciales sin re-registrarse).
 */
public enum TipoCliente {
    NUEVO,
    MIGRADO
}
