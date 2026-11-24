package com.aldemar.aquasample.exception;

/** Se lanza cuando se pide una muestra que no existe. Se responde con 404. */
public class MuestraNoEncontradaException extends RuntimeException {

    public MuestraNoEncontradaException(String id) {
        super("No existe una muestra con id " + id);
    }
}
