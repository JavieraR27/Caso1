package com.example.administrador.exception;


public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String mensaje) {
        super(mensaje);
    }

    public ServiceUnavailableException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
