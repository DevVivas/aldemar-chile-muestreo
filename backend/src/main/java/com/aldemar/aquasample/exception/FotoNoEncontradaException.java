package com.aldemar.aquasample.exception;

/** La muestra existe pero todavía no tiene foto. Se responde con 404. */
public class FotoNoEncontradaException extends RuntimeException {

    public FotoNoEncontradaException(String id) {
        super("La muestra " + id + " todavía no tiene foto");
    }
}
