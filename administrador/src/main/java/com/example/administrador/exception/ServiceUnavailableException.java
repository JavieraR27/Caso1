package com.example.administrador.exception;

/**
 * Servicio dependiente caído o inalcanzable (WebClient) → 503.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String mensaje) {
        super(mensaje);
    }

    public ServiceUnavailableException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
