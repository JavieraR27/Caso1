package com.example.administrador.exception;


public class BusinessConflictException extends RuntimeException {

    public BusinessConflictException(String mensaje) {
        super(mensaje);
    }

    public BusinessConflictException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
