package com.aldemar.aquasample.exception;

/**
 * Un OPERARIO intentó modificar (PUT) una muestra que en el servidor ya está VALIDADA.
 * Se responde con 409 y la muestra no se toca.
 */
public class MuestraYaValidadaException extends RuntimeException {

    public MuestraYaValidadaException() {
        super("La muestra ya fue validada por un supervisor");
    }
}
