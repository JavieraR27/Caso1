package com.example.clientes.exception;

/**
 * Regla de negocio violada (duplicado, estado inválido, credenciales incorrectas) → 409.
 */
public class BusinessConflictException extends RuntimeException {

    public BusinessConflictException(String mensaje) {
        super(mensaje);
    }

    public BusinessConflictException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
