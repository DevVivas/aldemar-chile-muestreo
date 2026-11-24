package com.aldemar.aquasample.exception;

/**
 * El usuario sí puede llamar al endpoint, pero la regla de negocio no le permite
 * esta acción concreta (por ejemplo, un operario intentando validar). Se responde con 403.
 */
public class AccionNoPermitidaException extends RuntimeException {

    public AccionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
